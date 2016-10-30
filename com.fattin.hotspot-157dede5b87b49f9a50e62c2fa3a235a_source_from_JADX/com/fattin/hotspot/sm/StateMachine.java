package com.fattin.hotspot.sm;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;
import org.acra.ACRAConstants;

public class StateMachine {
    public static final boolean HANDLED = true;
    public static final boolean NOT_HANDLED = false;
    public static final int SM_INIT_CMD = -2;
    public static final int SM_QUIT_CMD = -1;
    private static final String TAG = "StateMachine";
    private String mName;
    private SmHandler mSmHandler;
    private HandlerThread mSmThread;

    public static class ProcessedMessageInfo {
        private State orgState;
        private State state;
        private int what;

        ProcessedMessageInfo(Message message, State state, State orgState) {
            update(message, state, orgState);
        }

        public void update(Message message, State state, State orgState) {
            this.what = message.what;
            this.state = state;
            this.orgState = orgState;
        }

        public int getWhat() {
            return this.what;
        }

        public State getState() {
            return this.state;
        }

        public State getOriginalState() {
            return this.orgState;
        }

        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("what=");
            sb.append(this.what);
            sb.append(" state=");
            sb.append(cn(this.state));
            sb.append(" orgState=");
            sb.append(cn(this.orgState));
            return sb.toString();
        }

        private String cn(Object n) {
            if (n == null) {
                return "null";
            }
            String name = n.getClass().getName();
            return name.substring(name.lastIndexOf(36) + 1);
        }
    }

    private static class ProcessedMessages {
        private static final int DEFAULT_SIZE = 20;
        private int mCount;
        private int mMaxSize;
        private Vector<ProcessedMessageInfo> mMessages;
        private int mOldestIndex;

        ProcessedMessages() {
            this.mMessages = new Vector();
            this.mMaxSize = DEFAULT_SIZE;
            this.mOldestIndex = 0;
            this.mCount = 0;
        }

        void setSize(int maxSize) {
            this.mMaxSize = maxSize;
            this.mCount = 0;
            this.mMessages.clear();
        }

        int size() {
            return this.mMessages.size();
        }

        int count() {
            return this.mCount;
        }

        void cleanup() {
            this.mMessages.clear();
        }

        ProcessedMessageInfo get(int index) {
            int nextIndex = this.mOldestIndex + index;
            if (nextIndex >= this.mMaxSize) {
                nextIndex -= this.mMaxSize;
            }
            if (nextIndex >= size()) {
                return null;
            }
            return (ProcessedMessageInfo) this.mMessages.get(nextIndex);
        }

        void add(Message message, State state, State orgState) {
            this.mCount++;
            if (this.mMessages.size() < this.mMaxSize) {
                this.mMessages.add(new ProcessedMessageInfo(message, state, orgState));
                return;
            }
            ProcessedMessageInfo pmi = (ProcessedMessageInfo) this.mMessages.get(this.mOldestIndex);
            this.mOldestIndex++;
            if (this.mOldestIndex >= this.mMaxSize) {
                this.mOldestIndex = 0;
            }
            pmi.update(message, state, orgState);
        }
    }

    private static class SmHandler extends Handler {
        private static final Object mQuitObj;
        private boolean mDbg;
        private ArrayList<Message> mDeferredMessages;
        private State mDestState;
        private HaltingState mHaltingState;
        private State mInitialState;
        private boolean mIsConstructionCompleted;
        private Message mMsg;
        private ProcessedMessages mProcessedMessages;
        private QuittingState mQuittingState;
        private StateMachine mSm;
        private HashMap<State, StateInfo> mStateInfo;
        private StateInfo[] mStateStack;
        private int mStateStackTopIndex;
        private StateInfo[] mTempStateStack;
        private int mTempStateStackCount;

        private class StateInfo {
            boolean active;
            StateInfo parentStateInfo;
            State state;

            private StateInfo() {
            }

            public String toString() {
                return "state=" + this.state.getName() + ",active=" + this.active + ",parent=" + (this.parentStateInfo == null ? "null" : this.parentStateInfo.state.getName());
            }
        }

        private class HaltingState extends State {
            private HaltingState() {
            }

            public boolean processMessage(Message msg) {
                SmHandler.this.mSm.haltedProcessMessage(msg);
                return StateMachine.HANDLED;
            }
        }

        private class QuittingState extends State {
            private QuittingState() {
            }

            public boolean processMessage(Message msg) {
                return StateMachine.NOT_HANDLED;
            }
        }

        static {
            mQuitObj = new Object();
        }

        public final void handleMessage(Message msg) {
            if (this.mDbg) {
                Log.d(StateMachine.TAG, "handleMessage: E msg.what=" + msg.what);
            }
            this.mMsg = msg;
            if (this.mIsConstructionCompleted) {
                processMsg(msg);
                performTransitions();
                if (this.mDbg) {
                    Log.d(StateMachine.TAG, "handleMessage: X");
                    return;
                }
                return;
            }
            Log.e(StateMachine.TAG, "The start method not called, ignore msg: " + msg);
        }

        private void performTransitions() {
            State destState = null;
            while (this.mDestState != null) {
                if (this.mDbg) {
                    Log.d(StateMachine.TAG, "handleMessage: new destination call exit");
                }
                destState = this.mDestState;
                this.mDestState = null;
                invokeExitMethods(setupTempStateStackWithStatesToEnter(destState));
                invokeEnterMethods(moveTempStateStackToStateStack());
                moveDeferredMessageAtFrontOfQueue();
            }
            if (destState == null) {
                return;
            }
            if (destState == this.mQuittingState) {
                cleanupAfterQuitting();
            } else if (destState == this.mHaltingState) {
                this.mSm.halting();
            }
        }

        private final void cleanupAfterQuitting() {
            this.mSm.quitting();
            if (this.mSm.mSmThread != null) {
                getLooper().quit();
                this.mSm.mSmThread = null;
            }
            this.mSm.mSmHandler = null;
            this.mSm = null;
            this.mMsg = null;
            this.mProcessedMessages.cleanup();
            this.mStateStack = null;
            this.mTempStateStack = null;
            this.mStateInfo.clear();
            this.mInitialState = null;
            this.mDestState = null;
            this.mDeferredMessages.clear();
        }

        private final void completeConstruction() {
            if (this.mDbg) {
                Log.d(StateMachine.TAG, "completeConstruction: E");
            }
            int maxDepth = 0;
            for (StateInfo i : this.mStateInfo.values()) {
                int depth = 0;
                StateInfo i2;
                while (i2 != null) {
                    i2 = i2.parentStateInfo;
                    depth++;
                }
                if (maxDepth < depth) {
                    maxDepth = depth;
                }
            }
            if (this.mDbg) {
                Log.d(StateMachine.TAG, "completeConstruction: maxDepth=" + maxDepth);
            }
            this.mStateStack = new StateInfo[maxDepth];
            this.mTempStateStack = new StateInfo[maxDepth];
            setupInitialStateStack();
            this.mIsConstructionCompleted = StateMachine.HANDLED;
            this.mMsg = obtainMessage(StateMachine.SM_INIT_CMD);
            invokeEnterMethods(0);
            performTransitions();
            if (this.mDbg) {
                Log.d(StateMachine.TAG, "completeConstruction: X");
            }
        }

        private final void processMsg(Message msg) {
            StateInfo curStateInfo = this.mStateStack[this.mStateStackTopIndex];
            if (this.mDbg) {
                Log.d(StateMachine.TAG, "processMsg: " + curStateInfo.state.getName());
            }
            while (!curStateInfo.state.processMessage(msg)) {
                curStateInfo = curStateInfo.parentStateInfo;
                if (curStateInfo == null) {
                    this.mSm.unhandledMessage(msg);
                    if (isQuit(msg)) {
                        transitionTo(this.mQuittingState);
                    }
                    if (curStateInfo == null) {
                        this.mProcessedMessages.add(msg, curStateInfo.state, this.mStateStack[this.mStateStackTopIndex].state);
                    }
                    this.mProcessedMessages.add(msg, null, null);
                    return;
                } else if (this.mDbg) {
                    Log.d(StateMachine.TAG, "processMsg: " + curStateInfo.state.getName());
                }
            }
            if (curStateInfo == null) {
                this.mProcessedMessages.add(msg, null, null);
                return;
            }
            this.mProcessedMessages.add(msg, curStateInfo.state, this.mStateStack[this.mStateStackTopIndex].state);
        }

        private final void invokeExitMethods(StateInfo commonStateInfo) {
            while (this.mStateStackTopIndex >= 0 && this.mStateStack[this.mStateStackTopIndex] != commonStateInfo) {
                State curState = this.mStateStack[this.mStateStackTopIndex].state;
                if (this.mDbg) {
                    Log.d(StateMachine.TAG, "invokeExitMethods: " + curState.getName());
                }
                curState.exit();
                this.mStateStack[this.mStateStackTopIndex].active = StateMachine.NOT_HANDLED;
                this.mStateStackTopIndex += StateMachine.SM_QUIT_CMD;
            }
        }

        private final void invokeEnterMethods(int stateStackEnteringIndex) {
            for (int i = stateStackEnteringIndex; i <= this.mStateStackTopIndex; i++) {
                if (this.mDbg) {
                    Log.d(StateMachine.TAG, "invokeEnterMethods: " + this.mStateStack[i].state.getName());
                }
                this.mStateStack[i].state.enter();
                this.mStateStack[i].active = StateMachine.HANDLED;
            }
        }

        private final void moveDeferredMessageAtFrontOfQueue() {
            for (int i = this.mDeferredMessages.size() + StateMachine.SM_QUIT_CMD; i >= 0; i += StateMachine.SM_QUIT_CMD) {
                Message curMsg = (Message) this.mDeferredMessages.get(i);
                if (this.mDbg) {
                    Log.d(StateMachine.TAG, "moveDeferredMessageAtFrontOfQueue; what=" + curMsg.what);
                }
                sendMessageAtFrontOfQueue(curMsg);
            }
            this.mDeferredMessages.clear();
        }

        private final int moveTempStateStackToStateStack() {
            int startingIndex = this.mStateStackTopIndex + 1;
            int j = startingIndex;
            for (int i = this.mTempStateStackCount + StateMachine.SM_QUIT_CMD; i >= 0; i += StateMachine.SM_QUIT_CMD) {
                if (this.mDbg) {
                    Log.d(StateMachine.TAG, "moveTempStackToStateStack: i=" + i + ",j=" + j);
                }
                this.mStateStack[j] = this.mTempStateStack[i];
                j++;
            }
            this.mStateStackTopIndex = j + StateMachine.SM_QUIT_CMD;
            if (this.mDbg) {
                Log.d(StateMachine.TAG, "moveTempStackToStateStack: X mStateStackTop=" + this.mStateStackTopIndex + ",startingIndex=" + startingIndex + ",Top=" + this.mStateStack[this.mStateStackTopIndex].state.getName());
            }
            return startingIndex;
        }

        private final StateInfo setupTempStateStackWithStatesToEnter(State destState) {
            this.mTempStateStackCount = 0;
            StateInfo curStateInfo = (StateInfo) this.mStateInfo.get(destState);
            do {
                StateInfo[] stateInfoArr = this.mTempStateStack;
                int i = this.mTempStateStackCount;
                this.mTempStateStackCount = i + 1;
                stateInfoArr[i] = curStateInfo;
                curStateInfo = curStateInfo.parentStateInfo;
                if (curStateInfo == null) {
                    break;
                }
            } while (!curStateInfo.active);
            if (this.mDbg) {
                Log.d(StateMachine.TAG, "setupTempStateStackWithStatesToEnter: X mTempStateStackCount=" + this.mTempStateStackCount + ",curStateInfo: " + curStateInfo);
            }
            return curStateInfo;
        }

        private final void setupInitialStateStack() {
            if (this.mDbg) {
                Log.d(StateMachine.TAG, "setupInitialStateStack: E mInitialState=" + this.mInitialState.getName());
            }
            StateInfo curStateInfo = (StateInfo) this.mStateInfo.get(this.mInitialState);
            this.mTempStateStackCount = 0;
            while (curStateInfo != null) {
                this.mTempStateStack[this.mTempStateStackCount] = curStateInfo;
                curStateInfo = curStateInfo.parentStateInfo;
                this.mTempStateStackCount++;
            }
            this.mStateStackTopIndex = StateMachine.SM_QUIT_CMD;
            moveTempStateStackToStateStack();
        }

        private final Message getCurrentMessage() {
            return this.mMsg;
        }

        private final IState getCurrentState() {
            return this.mStateStack[this.mStateStackTopIndex].state;
        }

        private final StateInfo addState(State state, State parent) {
            if (this.mDbg) {
                Log.d(StateMachine.TAG, "addStateInternal: E state=" + state.getName() + ",parent=" + (parent == null ? ACRAConstants.DEFAULT_STRING_VALUE : parent.getName()));
            }
            StateInfo parentStateInfo = null;
            if (parent != null) {
                parentStateInfo = (StateInfo) this.mStateInfo.get(parent);
                if (parentStateInfo == null) {
                    parentStateInfo = addState(parent, null);
                }
            }
            StateInfo stateInfo = (StateInfo) this.mStateInfo.get(state);
            if (stateInfo == null) {
                stateInfo = new StateInfo();
                this.mStateInfo.put(state, stateInfo);
            }
            if (stateInfo.parentStateInfo == null || stateInfo.parentStateInfo == parentStateInfo) {
                stateInfo.state = state;
                stateInfo.parentStateInfo = parentStateInfo;
                stateInfo.active = StateMachine.NOT_HANDLED;
                if (this.mDbg) {
                    Log.d(StateMachine.TAG, "addStateInternal: X stateInfo: " + stateInfo);
                }
                return stateInfo;
            }
            throw new RuntimeException("state already added");
        }

        private SmHandler(Looper looper, StateMachine sm) {
            super(looper);
            this.mDbg = StateMachine.NOT_HANDLED;
            this.mProcessedMessages = new ProcessedMessages();
            this.mStateStackTopIndex = StateMachine.SM_QUIT_CMD;
            this.mHaltingState = new HaltingState();
            this.mQuittingState = new QuittingState();
            this.mStateInfo = new HashMap();
            this.mDeferredMessages = new ArrayList();
            this.mSm = sm;
            addState(this.mHaltingState, null);
            addState(this.mQuittingState, null);
        }

        private final void setInitialState(State initialState) {
            if (this.mDbg) {
                Log.d(StateMachine.TAG, "setInitialState: initialState" + initialState.getName());
            }
            this.mInitialState = initialState;
        }

        private final void transitionTo(IState destState) {
            this.mDestState = (State) destState;
            if (this.mDbg) {
                Log.d(StateMachine.TAG, "StateMachine.transitionTo EX destState" + this.mDestState.getName());
            }
        }

        private final void deferMessage(Message msg) {
            if (this.mDbg) {
                Log.d(StateMachine.TAG, "deferMessage: msg=" + msg.what);
            }
            Message newMsg = obtainMessage();
            newMsg.copyFrom(msg);
            this.mDeferredMessages.add(newMsg);
        }

        private final void quit() {
            if (this.mDbg) {
                Log.d(StateMachine.TAG, "quit:");
            }
            sendMessage(obtainMessage(StateMachine.SM_QUIT_CMD, mQuitObj));
        }

        private final boolean isQuit(Message msg) {
            return (msg.what == StateMachine.SM_QUIT_CMD && msg.obj == mQuitObj) ? StateMachine.HANDLED : StateMachine.NOT_HANDLED;
        }

        private final boolean isDbg() {
            return this.mDbg;
        }

        private final void setDbg(boolean dbg) {
            this.mDbg = dbg;
        }

        private final void setProcessedMessagesSize(int maxSize) {
            this.mProcessedMessages.setSize(maxSize);
        }

        private final int getProcessedMessagesSize() {
            return this.mProcessedMessages.size();
        }

        private final int getProcessedMessagesCount() {
            return this.mProcessedMessages.count();
        }

        private final ProcessedMessageInfo getProcessedMessageInfo(int index) {
            return this.mProcessedMessages.get(index);
        }
    }

    private void initStateMachine(String name, Looper looper) {
        this.mName = name;
        this.mSmHandler = new SmHandler(this, null);
    }

    protected StateMachine(String name) {
        this.mSmThread = new HandlerThread(name);
        this.mSmThread.start();
        initStateMachine(name, this.mSmThread.getLooper());
    }

    protected StateMachine(String name, Looper looper) {
        initStateMachine(name, looper);
    }

    protected final void addState(State state, State parent) {
        this.mSmHandler.addState(state, parent);
    }

    protected final Message getCurrentMessage() {
        return this.mSmHandler.getCurrentMessage();
    }

    protected final IState getCurrentState() {
        return this.mSmHandler.getCurrentState();
    }

    protected final void addState(State state) {
        this.mSmHandler.addState(state, null);
    }

    protected final void setInitialState(State initialState) {
        this.mSmHandler.setInitialState(initialState);
    }

    protected final void transitionTo(IState destState) {
        this.mSmHandler.transitionTo(destState);
    }

    protected final void transitionToHaltingState() {
        this.mSmHandler.transitionTo(this.mSmHandler.mHaltingState);
    }

    protected final void deferMessage(Message msg) {
        this.mSmHandler.deferMessage(msg);
    }

    protected void unhandledMessage(Message msg) {
        if (this.mSmHandler.mDbg) {
            Log.e(TAG, this.mName + " - unhandledMessage: msg.what=" + msg.what);
        }
    }

    protected void haltedProcessMessage(Message msg) {
    }

    protected void halting() {
    }

    protected void quitting() {
    }

    public final String getName() {
        return this.mName;
    }

    public final void setProcessedMessagesSize(int maxSize) {
        this.mSmHandler.setProcessedMessagesSize(maxSize);
    }

    public final int getProcessedMessagesSize() {
        return this.mSmHandler.getProcessedMessagesSize();
    }

    public final int getProcessedMessagesCount() {
        return this.mSmHandler.getProcessedMessagesCount();
    }

    public final ProcessedMessageInfo getProcessedMessageInfo(int index) {
        return this.mSmHandler.getProcessedMessageInfo(index);
    }

    public final Handler getHandler() {
        return this.mSmHandler;
    }

    public final Message obtainMessage() {
        if (this.mSmHandler == null) {
            return null;
        }
        return Message.obtain(this.mSmHandler);
    }

    public final Message obtainMessage(int what) {
        if (this.mSmHandler == null) {
            return null;
        }
        return Message.obtain(this.mSmHandler, what);
    }

    public final Message obtainMessage(int what, Object obj) {
        if (this.mSmHandler == null) {
            return null;
        }
        return Message.obtain(this.mSmHandler, what, obj);
    }

    public final Message obtainMessage(int what, int arg1, int arg2) {
        if (this.mSmHandler == null) {
            return null;
        }
        return Message.obtain(this.mSmHandler, what, arg1, arg2);
    }

    public final Message obtainMessage(int what, int arg1, int arg2, Object obj) {
        if (this.mSmHandler == null) {
            return null;
        }
        return Message.obtain(this.mSmHandler, what, arg1, arg2, obj);
    }

    public final void sendMessage(int what) {
        if (this.mSmHandler != null) {
            this.mSmHandler.sendMessage(obtainMessage(what));
        }
    }

    public final void sendMessage(int what, Object obj) {
        if (this.mSmHandler != null) {
            this.mSmHandler.sendMessage(obtainMessage(what, obj));
        }
    }

    public final void sendMessage(Message msg) {
        if (this.mSmHandler != null) {
            this.mSmHandler.sendMessage(msg);
        }
    }

    public final void sendMessageDelayed(int what, long delayMillis) {
        if (this.mSmHandler != null) {
            this.mSmHandler.sendMessageDelayed(obtainMessage(what), delayMillis);
        }
    }

    public final void sendMessageDelayed(int what, Object obj, long delayMillis) {
        if (this.mSmHandler != null) {
            this.mSmHandler.sendMessageDelayed(obtainMessage(what, obj), delayMillis);
        }
    }

    public final void sendMessageDelayed(Message msg, long delayMillis) {
        if (this.mSmHandler != null) {
            this.mSmHandler.sendMessageDelayed(msg, delayMillis);
        }
    }

    protected final void sendMessageAtFrontOfQueue(int what, Object obj) {
        this.mSmHandler.sendMessageAtFrontOfQueue(obtainMessage(what, obj));
    }

    protected final void sendMessageAtFrontOfQueue(int what) {
        this.mSmHandler.sendMessageAtFrontOfQueue(obtainMessage(what));
    }

    protected final void sendMessageAtFrontOfQueue(Message msg) {
        this.mSmHandler.sendMessageAtFrontOfQueue(msg);
    }

    protected final void removeMessages(int what) {
        this.mSmHandler.removeMessages(what);
    }

    public final void quit() {
        if (this.mSmHandler != null) {
            this.mSmHandler.quit();
        }
    }

    protected final boolean isQuit(Message msg) {
        return this.mSmHandler.isQuit(msg);
    }

    public boolean isDbg() {
        if (this.mSmHandler == null) {
            return NOT_HANDLED;
        }
        return this.mSmHandler.isDbg();
    }

    public void setDbg(boolean dbg) {
        if (this.mSmHandler != null) {
            this.mSmHandler.setDbg(dbg);
        }
    }

    public void start() {
        if (this.mSmHandler != null) {
            this.mSmHandler.completeConstruction();
        }
    }
}
