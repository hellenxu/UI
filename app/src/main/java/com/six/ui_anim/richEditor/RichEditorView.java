package com.six.ui_anim.richEditor;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * @author hellenxu
 * @date 2016/1/28
 * Copyright 2016 Six. All rights reserved.
 */

public class RichEditorView extends WebView {
    private static final String SETUP_HTML = "file:///android_asset/editor.html";
    private static final String CALLBACK_SCHEME = "re-callback://";
    private static final String STATE_SCHEME = "re-state://";
    private boolean isReady = false;
    private String mContents;
    private OnTextChangeListener mTextChangeListener;
    private OnDecorationStateListener mDecorationStateListener;
    private AfterInitialLoadListener mLoadListener;

    public interface OnTextChangeListener {
        void onTextChange(String text);
    }

    public interface OnDecorationStateListener {
        void onStateChangeListener(String text, List<FormatType> types);
    }

    public interface AfterInitialLoadListener {
        void onAfterInitialLoad(boolean isReady);
    }

    public RichEditorView(Context context) {
        this(context, null);
    }

    public RichEditorView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RichEditorView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setVerticalScrollBarEnabled(false);
        setHorizontalScrollBarEnabled(false);
        getSettings().setJavaScriptEnabled(true);
        setWebChromeClient(new WebChromeClient());
        setWebViewClient(createWebviewClient());
        loadUrl(SETUP_HTML);

        applyAttributes(context, attrs);
    }

    protected EditorWebViewClient createWebviewClient() {
        return new EditorWebViewClient();
    }

    public void setOnTextChangeListener(OnTextChangeListener listener) {
        this.mTextChangeListener = listener;
    }

    public void setOnDecorationChangeListener(OnDecorationStateListener listener) {
        this.mDecorationStateListener = listener;
    }

    public void setOnInitialLoadListener(AfterInitialLoadListener listener) {
        this.mLoadListener = listener;
    }

    //removing the prefix of contents
    private void callback(String text) {
        mContents = text.replaceFirst(CALLBACK_SCHEME, "");
        if (mTextChangeListener != null) {
            mTextChangeListener.onTextChange(mContents);
        }
    }

    //checking how many kinds of formats
    private void stateCheck(String text) {
        String state = text.replaceFirst(STATE_SCHEME, "").toUpperCase(Locale.ENGLISH);
        List<FormatType> types = new ArrayList<>();
        for (FormatType type : FormatType.values()) {
            if (TextUtils.indexOf(state, type.name()) != -1) {
                types.add(type);
            }
        }

        if (mDecorationStateListener != null) {
            mDecorationStateListener.onStateChangeListener(state, types);
        }
    }

    private void applyAttributes(Context context, AttributeSet attrs) {
        final int[] attrsArray = new int[]{android.R.attr.gravity};
        TypedArray ta = context.obtainStyledAttributes(attrs, attrsArray);

        int gravity = ta.getInt(0, NO_ID);
        switch (gravity) {
            case Gravity.LEFT:
                exec("javascript:RE.setTextAlign(\"left\")");
                break;
            case Gravity.RIGHT:
                exec("javascript:RE.setTextAlign(\"right\")");
                break;
            case Gravity.TOP:
                exec("javascript:RE.setVerticalAlign(\"top\")");
                break;
            case Gravity.BOTTOM:
                exec("javascript:RE.setVerticalAlign(\"bottom\")");
                break;
            case Gravity.CENTER:
                exec("javascript:RE.setTextAlign(\"center\")");
                exec("javascript:RE.setVerticalAlign(\"middle\")");
                break;
            case Gravity.CENTER_HORIZONTAL:
                exec("javascript:RE.setTextAlign(\"center\")");
                break;
            case Gravity.CENTER_VERTICAL:
                exec("javascript:RE.setVerticalAlign(\"middle\")");
                break;
        }
        ta.recycle();
    }

    public void setHtml(String contents) {
        if (contents == null) {
            contents = "";
        }
        try {
            exec("javascript:RE.setHtml('" + URLEncoder.encode(contents, "utf-8") + "');");
        } catch (UnsupportedEncodingException e) {
            Log.e("unsupportEncodingExc", e.toString());
        }
        mContents = contents;
    }

    public String getHtml() {
        return mContents;
    }

    public void setEditorFontColor(int color) {
        String hex = convertHexColorString(color);
        exec("javascript:RE.setBaseTextColor('" + hex + "');");
    }

    public void setEditorFontSize(int px){
        exec("javascript:RE.setBaseFontSize('" + px + "px');");
    }

    @Override
    public void setPadding(int left, int top, int right, int bottom) {
        super.setPadding(left, top, right, bottom);
        exec("javascript:RE.setPadding('" + left + "px', '" + top + "px', '" + right + "px', '" + bottom + "px');");
    }

    @Override
    public void setPaddingRelative(int start, int top, int end, int bottom) {
        setPadding(start, top, end, bottom);
    }

    public void setEditorBackgroundColor(int color){
        setBackgroundColor(color);
    }

    @Override
    public void setBackgroundResource(int resid) {
        Bitmap bitmap = Utils.decodeResource(getContext(), resid);
        String base64 = Utils.toBase64(bitmap);
        bitmap.recycle();

        exec("javascript:RE.setBackgroundImage('url(data:image/png;base64," + base64 + ")');");
    }

    @Override
    public void setBackground(Drawable background){
        Bitmap bitmap = Utils.toBitmap(background);
        String base64 = Utils.toBase64(bitmap);
        bitmap.recycle();

        exec("javascript:RE.setBackgroundImage('url(data:image/png;base64," + base64 + ")');");
    }

    public void setBackground(String url) {
        exec("javascript:RE.setBackgroundImage('url(" + url + ")');");
    }

    public void setEditorWidth(int width) {
        exec("javascript:RE.setWidth(" + width + "px');");
    }

    public void setEditorHeight(int height) {
        exec("javascript:RE.setHeight(" + height + "px');");
    }

    public void setPlaceHolder(String placeHolder) {
        exec("javascript:RE.setPlaceholder('" + placeHolder + "');");
    }

    public void loadCSS(String cssFile){
        String jsCSSImport = "(function() {" +
                "   var head = document.getElementsByTagName(\"head\")[0];" +
                "   var link = document.createElement(\"link\");" +
                "   link.rel = \"stylesheet\";" +
                "   link.type = \"text/css\";" +
                "   link.href = \"" + cssFile + "\";" +
                "   link.media = \"all\";" +
                "   head.appendChild(link);" +
                "}) ();";
        exec("javascript:" + jsCSSImport + "");
    }

    public void undo(){
        exec("javascript:RE.undo();");
    }

    public void redo(){
        exec("javascript:RE.redo();");
    }

    public void setBold(){
        exec("javascript:RE.setBold();");
    }

    public void setItalic(){
        exec("javascript:RE.setItalic();");
    }

    public void setSubscript(){
        exec("javascript:RE.setSubscript();");
    }

    public void setSuperscript(){
        exec("javascript:RE.setSuperscript();");
    }

    public void setStrikeThrough(){
        exec("javascript:RE.setStrikeThrough();");
    }

    public void setUnderline(){
        exec("javascript:RE.setUnderline();");
    }

    public void setTextColor(int color){
        exec("javascript:RE.prepareInset();");
        String hex = convertHexColorString(color);
        exec("javascript:RE.setTextColor('" + hex + "');");
    }

    public void setTextBackgroundColor(int color){
        exec("javascript:RE.prepareInsert();");
        String hex = convertHexColorString(color);
        exec("javascript:RE.setTextBackgroundColor('" + hex + "');");
    }

    private String convertHexColorString(int color){
        return String.format("#%06X", (0xFFFFFF & color));
    }

    protected void exec(final String trigger) {
        if(isReady){
            load(trigger);
        } else {
            postDelayed(new Runnable() {
                @Override
                public void run() {
                    exec(trigger);
                }
            }, 100);
        }
    }

    private void load(String trigger) {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT){
            evaluateJavascript(trigger, null);
        } else {
            loadUrl(trigger);
        }
    }

    //TODO
    protected class EditorWebViewClient extends WebViewClient {

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);

        }
    }
}
