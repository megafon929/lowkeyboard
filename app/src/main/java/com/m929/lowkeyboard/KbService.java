package com.m929.lowkeyboard;

import android.inputmethodservice.InputMethodService;
import android.text.InputType;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.InputMethodManager;

public class KbService extends InputMethodService implements KbView.Listener {

    private KbView view;
    private EditorInfo info;

    @Override
    public View onCreateInputView() {
        view = new KbView(this);
        view.setListener(this);
        return view;
    }

    @Override
    public void onStartInputView(EditorInfo ei, boolean restarting) {
        super.onStartInputView(ei, restarting);
        info = ei;
        if (view == null) return;
        view.refreshPrefs();

        int cls = ei.inputType & InputType.TYPE_MASK_CLASS;
        view.setMode(cls == InputType.TYPE_CLASS_NUMBER || cls == InputType.TYPE_CLASS_PHONE ? 1 : 0);

        int kind = KbView.ENTER_RETURN;
        if ((ei.imeOptions & EditorInfo.IME_FLAG_NO_ENTER_ACTION) == 0) {
            switch (ei.imeOptions & EditorInfo.IME_MASK_ACTION) {
                case EditorInfo.IME_ACTION_SEARCH: kind = KbView.ENTER_SEARCH; break;
                case EditorInfo.IME_ACTION_GO:
                case EditorInfo.IME_ACTION_NEXT: kind = KbView.ENTER_NEXT; break;
                case EditorInfo.IME_ACTION_DONE: kind = KbView.ENTER_DONE; break;
                case EditorInfo.IME_ACTION_SEND: kind = KbView.ENTER_SEND; break;
                default: break;
            }
        }
        view.setEnterKind(kind);
        autoCaps();
    }

    @Override
    public void onUpdateSelection(int oldSelStart, int oldSelEnd, int newSelStart, int newSelEnd,
                                  int candidatesStart, int candidatesEnd) {
        super.onUpdateSelection(oldSelStart, oldSelEnd, newSelStart, newSelEnd, candidatesStart, candidatesEnd);
        autoCaps(); // курсор переехал - пересчитываем регистр
    }

    @Override
    public void onText(String s) {
        InputConnection ic = getCurrentInputConnection();
        if (ic == null) return;
        ic.commitText(s, 1);
        autoCaps();
    }

    @Override
    public void onAction(int code) {
        InputConnection ic = getCurrentInputConnection();
        switch (code) {
            case KbView.DEL:
                delete(ic);
                autoCaps();
                break;
            case KbView.SPACE:
                if (ic != null) ic.commitText(" ", 1);
                autoCaps();
                break;
            case KbView.ENTER:
                enter(ic);
                autoCaps();
                break;
            case KbView.PICKER:
                InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                if (imm != null) imm.showInputMethodPicker();
                break;
            default:
                break;
        }
    }

    /**
     * Удаление символа. Сначала пробуем InputConnection (надёжнее в WebView и полях
     * с composing text), и только если редактор отказал - обычные key events.
     */
    private void delete(InputConnection ic) {
        if (ic == null) {
            sendDownUpKeyEvents(KeyEvent.KEYCODE_DEL);
            return;
        }
        CharSequence selected = ic.getSelectedText(0);
        if (selected != null && selected.length() > 0) {
            ic.commitText("", 1); // выделение заменяем пустой строкой
            return;
        }
        if (!ic.deleteSurroundingText(1, 0)) sendDownUpKeyEvents(KeyEvent.KEYCODE_DEL);
    }

    private void enter(InputConnection ic) {
        if (ic == null) return;
        if (info != null && (info.imeOptions & EditorInfo.IME_FLAG_NO_ENTER_ACTION) == 0) {
            int action = info.imeOptions & EditorInfo.IME_MASK_ACTION;
            if (action != EditorInfo.IME_ACTION_NONE && action != EditorInfo.IME_ACTION_UNSPECIFIED) {
                ic.performEditorAction(action);
                return;
            }
        }
        sendDownUpKeyEvents(KeyEvent.KEYCODE_ENTER);
    }

    /** Заглавная буква в начале предложения, если поле это поддерживает. */
    private void autoCaps() {
        if (view == null || info == null || view.getShift() == 2) return;
        InputConnection ic = getCurrentInputConnection();
        if (ic == null || info.inputType == InputType.TYPE_NULL) return;
        view.setShift(ic.getCursorCapsMode(info.inputType) != 0 ? 1 : 0);
    }
}
