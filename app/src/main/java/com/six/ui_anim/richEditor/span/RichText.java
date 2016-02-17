package com.six.ui_anim.richEditor.span;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.text.Editable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.style.BulletSpan;
import android.text.style.StyleSpan;
import android.text.style.URLSpan;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.EditText;

import com.six.ui_anim.R;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by xxl on 16/2/9.
 */

public class RichText extends EditText implements TextWatcher {
    private int bulletColor;
    private int bulletRadius;
    private int bulletGapWidth;
    private boolean historyEnable = true;
    private int historySize = 100;
    private int linkColor = 0;
    private boolean linkUnderline = true;
    private int quoteColor = 0;
    private int quoteStripeWidth = 0;
    private int quoteGapWidth = 0;

    private List<Editable> historyList = new LinkedList<>();
    private boolean isHistoryWorking = false;
    private int historyCursor = 0;

    private SpannableStringBuilder inputBefore;
    private Editable inputLast;

    public static final int FORMAT_BOLD = 0X01;
    public static final int FORMAT_ITALIC = 0X02;
    public static final int FORMAT_UNDERLINED = 0X03;
    public static final int FORMAT_STRIKETHROUGH = 0X04;
    public static final int FORMAT_BULLET = 0X05;
    public static final int FORMAT_QUOTE = 0X06;
    public static final int FORMAT_LINK = 0X07;

    public RichText(Context context) {
        this(context, null);
    }

    public RichText(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    private void init(AttributeSet attrs) {
        TypedArray array = getContext().obtainStyledAttributes(attrs, R.styleable.RichText);
        bulletColor = array.getColor(R.styleable.RichText_bulletColor, 0);
        bulletRadius = array.getDimensionPixelSize(R.styleable.RichText_bulletRadius, 0);
        bulletGapWidth = array.getDimensionPixelSize(R.styleable.RichText_bulletGapWidth, 0);
        array.recycle();

        if (historyEnable && historySize <= 0) {
            throw new IllegalArgumentException("historySize must > 0");
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        addTextChangedListener(this);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        removeTextChangedListener(this);
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        if (!historyEnable || isHistoryWorking) {
            return;
        }

        inputBefore = new SpannableStringBuilder(s);
    }

    @Override
    public void afterTextChanged(Editable s) {
        if (!historyEnable || isHistoryWorking) {
            return;
        }
        inputLast = new SpannableStringBuilder(s);
        if (s != null && s.toString().equals(inputBefore.toString())) {
            return;
        }
        if (historyList.size() >= historySize) {
            historyList.remove(0);
        }

        historyList.add(inputBefore);
        historyCursor = historyList.size();
    }

    @Override
    public void onTextChanged(CharSequence text, int start, int lengthBefore, int lengthAfter) {
        //do nothing
    }

    // the beginning of setting up Bullet
    public void bullet(boolean valid) {
        if (valid) {
            bulletValid();
        } else {
            bulletInvalid();
        }
    }

    protected void bulletValid() {
        String[] lines = TextUtils.split(getEditableText().toString(), "\n");
        for (int i = 0; i < lines.length; i++) {

            int lineStart = 0;
            for (int j = 0; j < i; j++) {
                lineStart = lineStart + lines[j].length() + 1;
            }
            int lineEnd = lineStart + lines[i].length();
            if (lineStart >= lineEnd) {
                continue;
            }

            int bulletStart = 0;
            int bulletEnd = 0;
            if (lineStart <= getSelectionStart() && getSelectionEnd() <= lineEnd) {
                bulletStart = lineStart;
                bulletEnd = lineEnd;
            } else if (getSelectionStart() <= lineStart && lineEnd <= getSelectionEnd()) {
                bulletStart = lineStart;
                bulletEnd = lineEnd;
            }

            if (bulletStart < bulletEnd) {
                getEditableText().setSpan(new RichBulletSpan(bulletColor, bulletRadius, bulletGapWidth), bulletStart, bulletEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        }
    }

    protected void bulletInvalid() {
        String[] lines = TextUtils.split(getEditableText().toString(), "\n");

        for (int i = 0; i < lines.length; i++) {
            if (!containBullet(i)) {
                continue;
            }

            int lineStart = 0;
            for (int j = 0; j < i; j++) {
                lineStart = lineStart + lines[j].length() + 1;
            }

            int lineEnd = lineStart + lines[i].length();
            if (lineStart >= lineEnd) {
                continue;
            }

            int bulletStart = 0;
            int bulletEnd = 0;
            if (lineStart <= getSelectionStart() && getSelectionEnd() <= lineEnd) {
                bulletStart = lineStart;
                bulletEnd = lineEnd;
            } else if (getSelectionStart() <= lineStart && lineEnd <= getSelectionEnd()) {
                bulletStart = lineStart;
                bulletEnd = lineEnd;
            }

            if (bulletStart < bulletEnd) {
                BulletSpan[] spans = getEditableText().getSpans(bulletStart, bulletEnd, BulletSpan.class);
                for (BulletSpan span : spans) {
                    getEditableText().removeSpan(span);
                }
            }
        }
    }

    protected boolean containBullet() {
        String[] lines = TextUtils.split(getEditableText().toString(), "\n");
        List<Integer> list = new ArrayList<>();

        for (int i = 0; i < lines.length; i++) {
            int lineStart = 0;
            for (int j = 0; j < i; j++) {
                lineStart = lineStart + lines[j].length() + 1;
            }
            int lineEnd = lineStart + lines[i].length();
            if (lineStart >= lineEnd) {
                continue;
            }

            if (lineStart <= getSelectionStart() && getSelectionEnd() <= lineEnd) {
                list.add(i);
            } else if (getSelectionStart() <= lineStart && lineEnd <= getSelectionEnd()) {
                list.add(i);
            }
        }

        for (Integer i : list) {
            if (!containBullet(i)) {
                return false;
            }
        }

        return true;
    }
    //end of setting up bullet.

    //start setting up bold style
    public void bold(boolean valid) {
        if (valid) {
            styleValid(Typeface.BOLD, getSelectionStart(), getSelectionEnd());
        } else {
            styleInvalid(Typeface.BOLD, getSelectionStart(), getSelectionEnd());
        }
    }

    protected void styleValid(int style, int start, int end) {
        switch (style) {
            case Typeface.NORMAL:
            case Typeface.BOLD:
            case Typeface.ITALIC:
            case Typeface.BOLD_ITALIC:
                if (start >= end) {
                    return;
                }
                getEditableText().setSpan(new StyleSpan(style), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                break;
            default:
                Log.d("styleValid", "invalid");
                break;
        }
    }

    protected void styleInvalid(int style, int start, int end) {
        switch (style) {
            case Typeface.NORMAL:
            case Typeface.BOLD:
            case Typeface.ITALIC:
            case Typeface.BOLD_ITALIC:
                if (start >= end) {
                    return;
                }
                StyleSpan[] spans = getEditableText().getSpans(start, end, StyleSpan.class);
                List<RichPart> list = new ArrayList<>();
                for (StyleSpan span : spans) {
                    if (span.getStyle() == style) {
                        list.add(new RichPart(getEditableText().getSpanStart(span), getEditableText().getSpanEnd(span)));
                        getEditableText().removeSpan(span);
                    }
                }

                for (RichPart part : list) {
                    if (part.isValid()) {
                        if (part.start < start) {
                            styleValid(style, part.start, start);
                        }

                        if (part.end > end) {
                            styleValid(style, end, part.end);
                        }
                    }
                }
                break;
            default:
                Log.d("styleInValid", "invalid");
                break;
        }
    }

    protected boolean containStyle(int style, int start, int end) {
        switch (style) {
            case Typeface.NORMAL:
            case Typeface.BOLD:
            case Typeface.ITALIC:
            case Typeface.BOLD_ITALIC:
                if (start > end) {
                    return false;
                }

                if (start == end) {
                    if (start - 1 < 0 || start + 1 > getEditableText().length()) {
                        return false;
                    } else {
                        StyleSpan[] before = getEditableText().getSpans(start - 1, start, StyleSpan.class);
                        StyleSpan[] after = getEditableText().getSpans(start, start + 1, StyleSpan.class);
                        return before.length > 0 && after.length > 0 && before[0].getStyle() == style && after[0].getStyle() == style;
                    }
                } else {
                    StringBuilder builder = new StringBuilder();
                    for (int i = start; i < end; i++) {
                        StyleSpan[] spans = getEditableText().getSpans(i, i + 1, StyleSpan.class);
                        for (StyleSpan span : spans) {
                            if (span.getStyle() == style) {
                                builder.append(getEditableText().subSequence(i, i + 1).toString());
                                break;
                            }
                        }
                    }
                    return getEditableText().subSequence(start, end).toString().equals(builder.toString());
                }
            default:
                return false;
        }
    }

    //end of setting up bold style


    //start setting up italic style
    public void italic(boolean valid){
        if(valid){
            styleValid(Typeface.ITALIC, getSelectionStart(), getSelectionEnd());
        }else {
            styleInvalid(Typeface.ITALIC, getSelectionStart(), getSelectionEnd());
        }
    }


    //end of setting up italic style
    public void link(String link) {
        link(link, getSelectionStart(), getSelectionEnd());
    }

    public void link(String link, int start, int end) {
        if (link != null && !TextUtils.isEmpty(link.trim())) {
            linkValid(link, start, end);
        } else {
            linkInvalid(start, end);
        }
    }

    protected void linkValid(String link, int start, int end) {
        if (start >= end) {
            return;
        }

        linkInvalid(start, end);
        getEditableText().setSpan(new RichURLSpan(link, linkColor, linkUnderline), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
    }

    protected void linkInvalid(int start, int end) {
        if (start >= end) {
            return;
        }

        URLSpan[] spans = getEditableText().getSpans(start, end, URLSpan.class);
        for (URLSpan span : spans) {
            getEditableText().removeSpan(span);
        }
    }

    protected boolean containBullet(int index) {
        String[] lines = TextUtils.split(getEditableText().toString(), "\n");
        if (index < 0 || index >= lines.length) {
            return false;
        }

        int start = 0;
        for (int i = 0; i < index; i++) {
            start = start + lines[i].length() + 1;
        }

        int end = start + lines[index].length();
        if (start >= end) {
            return false;
        }

        BulletSpan[] spans = getEditableText().getSpans(start, end, BulletSpan.class);
        return spans.length > 0;
    }

    protected void switchToRichStyle(Editable editable, int start, int end) {
        BulletSpan[] bulletSpans = editable.getSpans(start, end, BulletSpan.class);
        for (BulletSpan span : bulletSpans) {
            int spanStart = editable.getSpanStart(span);
            int spanEnd = editable.getSpanEnd(end);
            spanEnd = 0 < spanEnd && spanEnd < editable.length() && editable.charAt(spanEnd) == '\n' ? spanEnd - 1 : spanEnd;
            editable.removeSpan(span);
            editable.setSpan(new RichBulletSpan(bulletColor, bulletRadius, bulletGapWidth), spanStart, spanEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        URLSpan[] urlSpans = editable.getSpans(start, end, URLSpan.class);
        for (URLSpan span : urlSpans) {
            int spanStart = editable.getSpanStart(span);
            int spanEnd = editable.getSpanEnd(span);
            editable.removeSpan(span);
            editable.setSpan(new RichURLSpan(span.getURL(), linkColor, linkUnderline), spanStart, spanEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
    }

    public boolean contains(int format) {
        switch (format) {
            case FORMAT_BULLET:
                return containBullet();
            case FORMAT_BOLD:
                return containStyle(Typeface.BOLD, getSelectionStart(), getSelectionEnd());
            default:
                return false;
        }
    }
}
