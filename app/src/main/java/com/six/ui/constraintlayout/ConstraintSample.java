package com.six.ui.constraintlayout;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.ConstraintSet;
import android.transition.TransitionManager;
import android.view.View;
import android.widget.Button;

import com.six.ui.R;

/**
 * @CopyRight six.ca
 * Created by Heavens on 2018-02-16.
 */

public class ConstraintSample extends Activity {
    private ConstraintSet applySet = new ConstraintSet();
    private ConstraintSet centerSet = new ConstraintSet();
    private ConstraintSet resetSet = new ConstraintSet();
    private ConstraintSet childSet = new ConstraintSet();
    private ConstraintLayout ctlay;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_constraint_code_sample);
        ctlay = (ConstraintLayout) findViewById(R.id.ctlay);
        applySet.clone(ctlay); //clone is used to copy the original constraint set
        centerSet.clone(ctlay);
        resetSet.clone(ctlay);
    }

    public void applyConstraintSet(View view) {
        TransitionManager.beginDelayedTransition(ctlay);
        applySet.setMargin(R.id.btn_a, ConstraintSet.START, 20);
        applySet.applyTo(ctlay);
    }

    public void center(View view) {
        TransitionManager.beginDelayedTransition(ctlay);

        centerSet.constrainWidth(R.id.btn_a, 600);
        centerSet.constrainWidth(R.id.btn_b, 600);
        centerSet.constrainWidth(R.id.btn_c, 600);

        // use with ConstraintSet.START and ConstraintSet.END
        centerSet.centerHorizontallyRtl(R.id.btn_a, R.id.ctlay);
        centerSet.centerHorizontallyRtl(R.id.btn_b, R.id.ctlay);
        centerSet.centerHorizontallyRtl(R.id.btn_c, R.id.ctlay);

        // use with ConstraintSet.LEFT and ConstraintSet.RIGHT
//        centerSet.centerHorizontally(R.id.btn_a, R.id.ctlay);
//        centerSet.centerHorizontally(R.id.btn_b, R.id.ctlay);
//        centerSet.centerHorizontally(R.id.btn_c, R.id.ctlay);

        centerSet.applyTo(ctlay);
    }

    public void reset(View view) {
        TransitionManager.beginDelayedTransition(ctlay);
        resetSet.applyTo(ctlay);
    }

    public void onClickButtonA(View view) {
        TransitionManager.beginDelayedTransition(ctlay);

        childSet.setVisibility(R.id.btn_b, ConstraintSet.GONE);
        childSet.setVisibility(R.id.btn_c, ConstraintSet.GONE);

        Button btnApply = (Button) findViewById(R.id.btn_apply);
        childSet.clear(R.id.btn_a);
        childSet.connect(R.id.btn_a, ConstraintSet.START, R.id.ctlay, ConstraintSet.START, 0);
        childSet.connect(R.id.btn_a, ConstraintSet.END, R.id.ctlay, ConstraintSet.END, 0);
        childSet.connect(R.id.btn_a, ConstraintSet.TOP, R.id.ctlay, ConstraintSet.TOP, 0);
//        childSet.connect(R.id.btn_a, ConstraintSet.BOTTOM, R.id.ctlay, ConstraintSet.BOTTOM, 0);
        childSet.constrainHeight(R.id.btn_a, (int) btnApply.getY());
        childSet.constrainWidth(R.id.btn_a, ctlay.getMeasuredWidth());
        childSet.applyTo(ctlay);
    }
}
