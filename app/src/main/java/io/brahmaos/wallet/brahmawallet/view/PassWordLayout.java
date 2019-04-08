package io.brahmaos.wallet.brahmawallet.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.InputType;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.BaseInputConnection;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.InputMethodManager;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.List;

import io.brahmaos.wallet.brahmawallet.R;

public class PassWordLayout extends LinearLayout {

    private int maxLength = 6;

    private int inputIndex = 0;

    private List<String> mPassList;

    private pwdChangeListener pwdChangeListener;

    private Context mContext;

    private boolean mIsShowInputLine;
    private int mInputColor;
    private int mNoinputColor;
    private int mLineColor;
    private int mTxtInputColor;
    private int mDrawType;
    private int mInterval;
    private int mItemWidth;
    private int mItemHeight;
    private int mShowPassType;
    private int mTxtSize;
    private int mBoxLineSize;


    public void setPwdChangeListener(pwdChangeListener pwdChangeListener) {
        this.pwdChangeListener = pwdChangeListener;
    }

    public PassWordLayout(Context context) {
        this(context, null);
    }

    public PassWordLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PassWordLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context, attrs);
    }

    private void initView(Context context, AttributeSet attrs) {

        mContext = context;
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.PassWordLayoutStyle);

        mInputColor = ta.getResourceId(R.styleable.PassWordLayoutStyle_box_input_color, R.color.pass_view_rect_input);
        mNoinputColor = ta.getResourceId(R.styleable.PassWordLayoutStyle_box_no_input_color, R.color.regi_line_color);
        mLineColor = ta.getResourceId(R.styleable.PassWordLayoutStyle_input_line_color, R.color.pass_view_rect_input);
        mTxtInputColor = ta.getResourceId(R.styleable.PassWordLayoutStyle_text_input_color, R.color.pass_view_rect_input);
        mDrawType = ta.getInt(R.styleable.PassWordLayoutStyle_box_draw_type, 0);
        mInterval = ta.getDimensionPixelOffset(R.styleable.PassWordLayoutStyle_interval_width, 4);
        maxLength = ta.getInt(R.styleable.PassWordLayoutStyle_pass_leng, 6);
        mItemWidth = ta.getDimensionPixelOffset(R.styleable.PassWordLayoutStyle_item_width, 40);
        mItemHeight = ta.getDimensionPixelOffset(R.styleable.PassWordLayoutStyle_item_height, 40);
        mShowPassType = ta.getInt(R.styleable.PassWordLayoutStyle_pass_inputed_type, 0);
        mTxtSize = ta.getDimensionPixelOffset(R.styleable.PassWordLayoutStyle_draw_txt_size, 18);
        mBoxLineSize = ta.getDimensionPixelOffset(R.styleable.PassWordLayoutStyle_draw_box_line_size, 4);
        mIsShowInputLine = ta.getBoolean(R.styleable.PassWordLayoutStyle_is_show_input_line, true);
        ta.recycle();

        mPassList = new ArrayList<>();

        setOrientation(HORIZONTAL);
        setGravity(Gravity.CENTER);

        setOnClickListener(view -> {
            setFocusable(true);
            setFocusableInTouchMode(true);
            requestFocus();
            InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(PassWordLayout.this, InputMethodManager.SHOW_IMPLICIT);
        });
        this.setOnKeyListener(new MyKeyListener());

        setOnFocusChangeListener((view, b) -> {
            if (b) {
                PassWordView passWordView = (PassWordView) getChildAt(inputIndex);
                if (passWordView != null) {
                    passWordView.setmIsShowRemindLine(mIsShowInputLine);
                    passWordView.startInputState();
                }
            } else {
                PassWordView passWordView = (PassWordView) getChildAt(inputIndex);
                if (passWordView != null) {
                    passWordView.setmIsShowRemindLine(false);
                    passWordView.updateInputState(false);
                }
            }
        });
    }

    private void addChildViews(Context context) {
        for (int i = 0; i < maxLength; i++) {
            PassWordView passWordView = new PassWordView(context);
            LayoutParams params = new LayoutParams(mItemWidth, mItemHeight);
            // the first and the last don't add margin
            if (i > 0) {
                params.leftMargin = mInterval;
            }

            passWordView.setInputStateColor(mInputColor);
            passWordView.setNoinputColor(mNoinputColor);
            passWordView.setInputStateTextColor(mTxtInputColor);
            passWordView.setRemindLineColor(mLineColor);
            passWordView.setmBoxDrawType(mDrawType);
            passWordView.setmShowPassType(mShowPassType);
            passWordView.setmDrawTxtSize(mTxtSize);
            passWordView.setmDrawBoxLineSize(mBoxLineSize);
            passWordView.setmIsShowRemindLine(mIsShowInputLine);

            addView(passWordView, params);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        // compare the children width and the parent width
        if (getChildCount() == 0) {
            if ((maxLength * mItemWidth + (maxLength - 1) * mInterval) > getMeasuredWidth()) {
                mItemWidth = (getMeasuredWidth() - (maxLength - 1) * mInterval) / maxLength;
                mItemHeight = mItemWidth;
            }

            addChildViews(getContext());
        }
    }

    public void addPwd(String pwd) {
        if (mPassList != null && mPassList.size() < maxLength) {
            mPassList.add(pwd + "");
            setNextInput(pwd);
        }

        if (pwdChangeListener != null) {
            if (mPassList.size() < maxLength) {
                pwdChangeListener.onChange(getPassString());
            } else {
                pwdChangeListener.onFinished(getPassString());
            }
        }
    }

    public void removePwd() {
        if (mPassList != null && mPassList.size() > 0) {
            mPassList.remove(mPassList.size() - 1);
            setPreviosInput();
        }

        if (pwdChangeListener != null) {
            if (mPassList.size() > 0) {
                pwdChangeListener.onChange(getPassString());
            } else {
                pwdChangeListener.onNull();
            }
        }
    }

    public void removeAllPwd() {
        if (mPassList != null) {
            for (int i = mPassList.size(); i >= 0; i--) {
                if (i > 0) {
                    setNoInput(i, false, "");
                } else if (i == 0) {
                    PassWordView passWordView = (PassWordView) getChildAt(i);
                    if (passWordView != null) {
                        passWordView.setmPassText("");
                        passWordView.startInputState();
                    }
                }

            }

            mPassList.clear();
            inputIndex = 0;
        }


        if (pwdChangeListener != null) {
            pwdChangeListener.onNull();
        }
    }

    public String getPassString() {

        StringBuffer passString = new StringBuffer();

        for (String i : mPassList) {
            passString.append(i);
        }

        return passString.toString();
    }

    private void setNextInput(String pwdTxt) {
        if (inputIndex < maxLength) {
            setNoInput(inputIndex, true, pwdTxt);
            inputIndex++;
            PassWordView passWordView = (PassWordView) getChildAt(inputIndex);
            if (passWordView != null) {
                passWordView.setmPassText(pwdTxt + "");
                passWordView.startInputState();
            }
        }

    }

    private void setPreviosInput() {
        if (inputIndex > 0) {
            setNoInput(inputIndex, false, "");
            inputIndex--;
            PassWordView passWordView = (PassWordView) getChildAt(inputIndex);
            if (passWordView != null) {
                passWordView.setmPassText("");
                passWordView.startInputState();
            }
        } else if (inputIndex == 0) {
            PassWordView passWordView = (PassWordView) getChildAt(inputIndex);
            if (passWordView != null) {
                passWordView.setmPassText("");
                passWordView.startInputState();
            }
        }
    }

    public void setNoInput(int index, boolean isinput, String txt) {
        if (index < 0) {
            return;
        }
        PassWordView passWordView = (PassWordView) getChildAt(index);
        if (passWordView != null) {
            passWordView.setmPassText(txt);
            passWordView.updateInputState(isinput);
        }
    }

    public interface pwdChangeListener {
        void onChange(String pwd);

        void onNull();

        void onFinished(String pwd);
    }

    @Override
    public InputConnection onCreateInputConnection(EditorInfo outAttrs) {
        outAttrs.inputType = InputType.TYPE_CLASS_NUMBER;
        outAttrs.imeOptions = EditorInfo.IME_FLAG_NO_EXTRACT_UI;
        return new ZanyInputConnection(this, false);
    }

    private class ZanyInputConnection extends BaseInputConnection {
        @Override
        public boolean commitText(CharSequence txt, int newCursorPosition) {
            return super.commitText(txt, newCursorPosition);
        }

        public ZanyInputConnection(View targetView, boolean fullEditor) {
            super(targetView, fullEditor);
        }

        @Override
        public boolean sendKeyEvent(KeyEvent event) {
            return super.sendKeyEvent(event);
        }


        @Override
        public boolean deleteSurroundingText(int beforeLength, int afterLength) {
            if (beforeLength == 1 && afterLength == 0) {
                return sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DEL)) && sendKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_DEL));
            }

            return super.deleteSurroundingText(beforeLength, afterLength);
        }
    }

    class MyKeyListener implements OnKeyListener {
        @Override
        public boolean onKey(View v, int keyCode, KeyEvent event) {
            if (event.getAction() == KeyEvent.ACTION_DOWN) {
                if (event.isShiftPressed()) {
                    return false;
                }
                if (keyCode >= KeyEvent.KEYCODE_0 && keyCode <= KeyEvent.KEYCODE_9) {
                    addPwd(keyCode - 7 + "");
                    return true;
                }

                if (keyCode == KeyEvent.KEYCODE_DEL) {
                    removePwd();
                    return true;
                }

                InputMethodManager imm = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                return true;
            }
            return false;
        }
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (!(state instanceof SavedState)) {
            super.onRestoreInstanceState(state);
            return;
        }
        SavedState savedState = (SavedState) state;
        super.onRestoreInstanceState(savedState.getSuperState());
        this.mPassList = savedState.saveString;
        inputIndex = mPassList.size();
        if (mPassList.isEmpty()) {
            return;
        }
        for (int i = 0; i < getChildCount(); i++) {
            PassWordView passWordView = (PassWordView) getChildAt(i);
            if (i > mPassList.size() - 1) {
                if (passWordView != null) {
                    passWordView.setmIsShowRemindLine(false);
                    passWordView.updateInputState(false);
                }
                break;
            }

            if (passWordView != null) {
                passWordView.setmPassText(mPassList.get(i));
                passWordView.updateInputState(true);
            }
        }

    }

    @Override
    protected Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        SavedState savedState = new SavedState(superState);
        savedState.saveString = this.mPassList;
        return savedState;
    }


    public static class SavedState extends BaseSavedState {
        public List<String> saveString;

        public SavedState(Parcelable superState) {
            super(superState);
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);

            dest.writeList(saveString);
        }

        private SavedState(Parcel in) {
            super(in);
            in.readStringList(saveString);
        }

        public static final Creator<SavedState> CREATOR = new Creator<SavedState>() {
            @Override
            public SavedState createFromParcel(Parcel source) {
                return new SavedState(source);
            }

            @Override
            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
    }


}
