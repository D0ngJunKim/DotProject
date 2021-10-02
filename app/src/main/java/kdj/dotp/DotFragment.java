package kdj.dotp;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Dialog;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.DialogFragment;
import androidx.navigation.fragment.FragmentKt;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import kdj.dotp.widget.adapter.DotInnerAdapter;
import kdj.dotp.widget.adapter.DotOuterAdapter;
import kdj.dotp.widget.anim.DotOuterLayerCloseAnimation;
import kdj.dotp.widget.anim.DotOuterLayerOpenAnimation;
import kdj.dotp.widget.anim.DotViewCloseAnimation;
import kdj.dotp.widget.anim.DotViewOpenAnimation;
import kdj.dotp.widget.dao.FruitInfo;
import kdj.dotp.widget.listener.OnDotAnimCheckListener;
import kdj.dotp.widget.util.ExDimensionKt;
import kdj.dotp.widget.value.DotOpenAnimValue;
import kdj.dotp.widget.view.CircularGradationDecoration;
import kdj.dotp.widget.view.CircularLayoutManager;
import kdj.dotp.widget.view.CircularRecyclerView;

/**
 * Created by DJKim on 2019-03-27.
 */
public class DotFragment extends DialogFragment implements OnDotAnimCheckListener {
    public static final int MAX_DIAMETER = 420;
    private int CURRENT_DIAMETER;

    // Dim Back Panel
    private View vBackground;
    // Touch Blocking View
    private View vTouchBlock;
    // Outer Layer
    private View vOuterLayer;
    // Outer Title;
    private ConstraintLayout lyOuterTitle;
    // Outer Pager RecyclerView
    private RecyclerView rvOuter;
    // Circular RecyclerView
    private CircularRecyclerView rvInner;
    // Circular RecyclerView LayoutManager
    private CircularLayoutManager lmInner;

    // Detail Values
    private float mInnerCircleRadius;
    private float mInnerItemRadius;
    private float mHorizontalOffsetInPx;
    private float mDotWidthInPx;
    private int mShadowSizeInPx;
    private int mCloseSizeInPx;

    private DotOuterAdapter mOuterAdapter;
    private DotInnerAdapter mInnerAdapter;
    private View mRootDotIconLayout;
    private ImageView ivDotAllBg, ivClose, ivDotHomeIcon, ivDotShadow;
    private TextView tvStore;
    private CircularGradationDecoration mInnerGradient;

    private float mInnerItemAngle;

    // Anim Checker
    private boolean isInnerScaleFinished = false;
    private boolean isInnerItemAnimFinished = false;
    private boolean isDotAnimFinished = false;
    private boolean isOuterLayerAnimFinished = false;

    // Double Back Checker
    private boolean isBackPressed = false;

    private final ArrayList<FruitInfo> mFruitList = new ArrayList<>();

    public static DotFragment newInstance() {
        return new DotFragment();
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Back Key Event Setting.
        Dialog dialog = new Dialog(getActivity(), 0) {
            @Override
            public void onBackPressed() {
                if (!isBackPressed) {
                    isBackPressed = true;
                    dismissWithAnimation();
                }
            }
        };
        // Remove Dim & Background
        Window window = dialog.getWindow();
        if (window != null) {
            WindowManager.LayoutParams params = new WindowManager.LayoutParams();
            params.copyFrom(window.getAttributes());
            params.width = WindowManager.LayoutParams.MATCH_PARENT;
            params.height = WindowManager.LayoutParams.MATCH_PARENT;
            window.setAttributes(params);

            window.requestFeature(Window.FEATURE_NO_TITLE);
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            window.setDimAmount(0);
        }
        return dialog;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_dot, container, false);
        initView(rootView);
        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        showWithAnimation();
    }

    private void initView(View rootView) {
        initFruitList();

        // Value Setting
        initCircularVariables();

        // Touch Blocking View Setting
        vTouchBlock = rootView.findViewById(R.id.vTouchBlock);

        // Dim Background Setting
        initDimView(rootView);

        // View Pager Setting.
        initOuterViewPagerLayer(rootView);

        // Circular RecyclerView Setting
        initCircularRecyclerView(rootView);

        // Ssg Dot Setting
        initDotView(rootView);

        initAdapters();
    }

    private void initFruitList() {
        mFruitList.add(new FruitInfo("아보카도", R.drawable.ic_avocado));
        mFruitList.add(new FruitInfo("바나나", R.drawable.ic_bananas));
        mFruitList.add(new FruitInfo("체리", R.drawable.ic_cherries));
        mFruitList.add(new FruitInfo("망고", R.drawable.ic_mango));
        mFruitList.add(new FruitInfo("오렌지", R.drawable.ic_orange));
        mFruitList.add(new FruitInfo("딸기", R.drawable.ic_strawberry));
        mFruitList.add(new FruitInfo("수박", R.drawable.ic_watermelon));
    }

    private void initCircularVariables() {
        // MAX 값을 넘어가지 않은 경우, 자신의 Width를 이용하여 계산.
        int displayWidth = getResources().getConfiguration().screenWidthDp;

        if (displayWidth > (int) ExDimensionKt.toPx(getResources().getConfiguration().screenWidthDp, getContext())) {
            CURRENT_DIAMETER = MAX_DIAMETER;
        } else {
            CURRENT_DIAMETER = displayWidth;
        }

        mInnerCircleRadius = (CURRENT_DIAMETER * 1.0133f) / 2f;
        mInnerItemRadius = mInnerCircleRadius - (mInnerCircleRadius * 0.3f);

        float pxDiameter = ExDimensionKt.toPx(CURRENT_DIAMETER, getContext());
        float px375 = ExDimensionKt.toPx(375, getContext());
        float ratio = ExDimensionKt.toPx(24, getContext()) / px375;

        mHorizontalOffsetInPx = pxDiameter * ratio;

        ratio = ExDimensionKt.toPx(100, getContext()) / px375;
        mDotWidthInPx = pxDiameter * ratio;

        ratio = ExDimensionKt.toPx(140, getContext()) / px375;
        mShadowSizeInPx = Math.round(pxDiameter * ratio);

        ratio = ExDimensionKt.toPx(28, getContext()) / px375;
        mCloseSizeInPx = Math.round(pxDiameter * ratio);

        mInnerItemAngle = (float) (180 - (Math.toDegrees(Math.acos((pxDiameter * 0.09722) / ExDimensionKt.toPx(mInnerItemRadius, getContext()))) * 2));
    }

    private void initDimView(View rootView) {
        vBackground = rootView.findViewById(R.id.vDim);
        vBackground.setOnClickListener(v -> dismissWithAnimation());
    }

    private void initOuterViewPagerLayer(View rootView) {
        vOuterLayer = rootView.findViewById(R.id.vLayer);
        lyOuterTitle = rootView.findViewById(R.id.lyOuter);
        tvStore = rootView.findViewById(R.id.tvFruitStore);
        rvOuter = rootView.findViewById(R.id.rvOuter);
        mOuterAdapter = new DotOuterAdapter();
        rvOuter.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        rvOuter.setAdapter(mOuterAdapter);

        PagerSnapHelper snapHelper = new PagerSnapHelper();
        snapHelper.attachToRecyclerView(rvOuter);

        vOuterLayer.setAlpha(0);
        lyOuterTitle.setAlpha(0);
        rvOuter.setAlpha(0);
    }

    private void initCircularRecyclerView(View rootView) {
        // Circular RecyclerView Setting
        rvInner = rootView.findViewById(R.id.rvInner);

        // 원의 반지름을 세팅. 반지름으로부터 계산한 지름을 기준으로 RecyclerView의 Width, Height를 조정.
        rvInner.setCircleRadius(mInnerCircleRadius);
        int[] colorList = {0x00FFFFFF, 0x00FFFFFF, 0xB3FFFFFF, 0xFFFFFFFF, 0xFFFFFFFF};
        float[] positionList = {0f, 0.45f, 0.5f, 0.55f, 1f};

        mInnerGradient = new CircularGradationDecoration(ExDimensionKt.toPx(mInnerCircleRadius, getContext()),
                colorList, positionList);
        rvInner.setShadowDecoration(mInnerGradient);

        // Layout Manager 정의.
        lmInner = new CircularLayoutManager(getContext(), mInnerCircleRadius, mInnerItemRadius);

        // Option 세팅.
        // 1. Item이 "보여지는" 최대, 최소 각도 설정.
        //                  0
        //                  |
        //                  |
        //                  |
        // -90 --------------------------90
        //                  |
        //                  |
        //                  |
        //              (-)180
        // 위의 그림 기준. 마이너스 부호는 방향성. Ex) (-180, 180) => 0을 기준으로 왼쪽으로 180도 만큼, 오른쪽으로 180도 만큼만 보여주세요. 넘어가면 Recycle.
        lmInner.setDegreeRangeWillShow(-120, 120);

        // 2. 반지름이 디바이스 Width보다 커져서 스크롤이 불가능한 영역이 생길 시 왼쪽에 얼만큼의 Offset을 둘지 결정.
        // 이를 기준으로 Item이 "스크롤 될" 최대, 최소 각도를 자동으로 계산.
        lmInner.setArcScrollOffset(mHorizontalOffsetInPx);

        // 3. 각각의 Item 사잇각 세팅.
        lmInner.setIntervalAngle(Math.round(mInnerItemAngle));

        // 4. 초기값으로 쓰일 스크롤 최대, 최소 각도 설정.
        lmInner.setDefaultScrollLimitAngle(-70, 60);

        // 5. 안쪽 원에만 스크롤 각도의 Offset Angle 을 추가해준다.
        lmInner.setScrollLimitOffestAngle(-10, -10);

        // Layout Manager Setting.
        rvInner.setLayoutManager(lmInner);

        // Animation CallBack Setting.
        rvInner.setAnimationCallBack(mInnerAnimCallBack);

        mInnerAdapter = new DotInnerAdapter();

        mInnerAdapter.setCurrentDiameter(CURRENT_DIAMETER);

        rvInner.setAdapter(mInnerAdapter);
    }

    private void initDotView(View rootView) {
        mRootDotIconLayout = rootView.findViewById(R.id.vDot);
        mRootDotIconLayout.setOnClickListener(v -> dismissWithAnimation());
        ivDotAllBg = rootView.findViewById(R.id.vDotBg);
        ivClose = rootView.findViewById(R.id.vDotClose);
        ivDotHomeIcon = rootView.findViewById(R.id.vDotIcon);
        ivDotShadow = rootView.findViewById(R.id.v_dot_bg);
        initShadowViewSize();
    }

    private void initAdapters() {
        mOuterAdapter.setDataList(mFruitList);
        mInnerAdapter.setDataList(mFruitList);
    }

    private void showWithAnimation() {
        // 1. Init Animation End Checkers
        initAnimChecker();
        // 2. Dot Animation Starts
        DotViewOpenAnimation.getInstance().openDotView(getContext(), mRootDotIconLayout, ivDotAllBg, ivDotHomeIcon, ivClose, ivDotShadow, mDotWidthInPx, this);
        // 3. Block Possible Touch Event Input.
        vTouchBlock.setVisibility(View.VISIBLE);
        // 4. Background Alpha Animation
        beginAlphaInAnim();
        // 5. Outer RecyclerView Animation
        beginOuterInAnim();
        // 6. Inner RecyclerView Animation
        beginInnerInAnim();
    }

    private void dismissWithAnimation() {
        // 1. Init Animation End Checkers
        initAnimChecker();
        // 2. Stop Ssg Dot Animation.
        DotViewOpenAnimation.getInstance().releaseBreathingDotIconBgAnimation();
        // 3. close Ssg Dot
        DotViewCloseAnimation.getInstance().closeDotView(getContext(), vBackground, mRootDotIconLayout, ivDotAllBg, ivClose, ivDotHomeIcon, ivDotShadow, this);
        // 4. Stop Scroll
        rvInner.stopScroll();
        // 5. Block Possible Touch Event Input.
        vTouchBlock.setVisibility(View.VISIBLE);
        // 6. Outer RecyclerView Animation
        beginOuterOutAnim();
        // 7. Inner RecyclerView Animation
        beginInnerOutAnim();
    }

    private void beginOuterInAnim() {
        DotOuterLayerOpenAnimation.getInstance().openLayer(vOuterLayer, lyOuterTitle, rvOuter, this);
    }

    private void beginInnerInAnim() {
        rvInner.beginEnterAction();
    }

    private void beginOuterOutAnim() {
        DotOuterLayerCloseAnimation.getInstance().closeLayer(vOuterLayer, lyOuterTitle, rvOuter, this);
    }

    private void beginInnerOutAnim() {
        rvInner.beginExitAction();
    }

    /** Fragment Enter Animation */
    private void beginAlphaInAnim() {
        ValueAnimator alphaAnim = ObjectAnimator.ofFloat(vBackground, View.ALPHA, 0.0f, 0.8f);
        alphaAnim.setDuration(DotOpenAnimValue.BACKGROUND.getDuration());
        alphaAnim.setStartDelay(DotOpenAnimValue.BACKGROUND.getDelay());
        alphaAnim.setInterpolator(new DecelerateInterpolator());
        alphaAnim.start();
    }

    private void initAnimChecker() {
        isInnerScaleFinished = false;
        isOuterLayerAnimFinished = false;
        isInnerItemAnimFinished = false;
        isDotAnimFinished = false;
    }

    private void initCloseViewSize() {
        ivClose.getLayoutParams().width = mCloseSizeInPx;
        ivClose.getLayoutParams().height = mCloseSizeInPx;
    }

    private void initShadowViewSize() {
        ivDotShadow.getLayoutParams().width = mShadowSizeInPx;
        ivDotShadow.getLayoutParams().height = mShadowSizeInPx;
    }

    private void checkOpenValidity() {
        if (isOuterLayerAnimFinished && isInnerItemAnimFinished && isInnerScaleFinished && isDotAnimFinished) {
            if (vTouchBlock.getVisibility() != View.GONE) {
                vTouchBlock.setVisibility(View.GONE);
            }
        }
    }

    private void checkCloseValidity() {
        if (isOuterLayerAnimFinished && isInnerItemAnimFinished && isInnerScaleFinished && isDotAnimFinished) {
            FragmentKt.findNavController(this).navigate(R.id.action_DOT_to_MAIN);
        }
    }

    /** Foldable 대응 */
    private void rearrangeForFoldable() {
        // 가변 Value 계산.
        initCircularVariables();
        // 그려지는 원 크기 조정.
        rvInner.rearrangeRadius(mInnerCircleRadius);
        // 아이템이 그려지는 반지름 값 조정.
        lmInner.rearrangeItemCircle(mInnerCircleRadius, mInnerItemRadius);
        // 아이템 사잇각 조정.
        lmInner.setIntervalAngle(Math.round(mInnerItemAngle));
        // 하단 Fade Out 그라데이션 크기 조정.
        mInnerGradient.rearrangeRadius(mInnerCircleRadius);
        // 아이템 사이즈 가변으로 조정되는 기준 값 세팅.
        mInnerAdapter.setCurrentDiameter(CURRENT_DIAMETER);
        // 적용!
        mInnerAdapter.notifyDataSetChanged();
        mOuterAdapter.notifyDataSetChanged();
        // 적용!
        rvInner.requestLayout();
        // SsgDot 크기 조정.
        DotViewOpenAnimation.getInstance().resizeDotIconBg(getContext(), ivDotAllBg, mDotWidthInPx);
        // SsgDot Close 크기 조정.
        initCloseViewSize();
        // SsgDot Shadow 크기 조정.
        initShadowViewSize();
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        rearrangeForFoldable();
    }

    /** Animation 후처리 콜백 */
    CircularRecyclerView.OnCompleteCallback mInnerAnimCallBack = new CircularRecyclerView.OnCompleteCallback() {
        @Override
        public void onCompleteItemAnimIn() {
            isInnerItemAnimFinished = true;
            checkOpenValidity();
        }

        @Override
        public void onCompleteItemAnimOut() {
            isInnerItemAnimFinished = true;
            checkCloseValidity();
        }

        @Override
        public void onCompleteScaleExpand() {
            isInnerScaleFinished = true;

            checkOpenValidity();
        }

        @Override
        public void onCompleteScaleCollapse() {
            isInnerScaleFinished = true;
            checkCloseValidity();
        }
    };

    @Override
    public void onDotAnimFinished(boolean isEnter) {
        isDotAnimFinished = true;
        if (isEnter) {
            checkOpenValidity();
        } else {
            checkCloseValidity();
        }
    }

    @Override
    public void onLayerAnimFinished(boolean isEnter) {
        isOuterLayerAnimFinished = true;
        if (isEnter) {
            checkOpenValidity();
        } else {
            checkCloseValidity();
        }
    }
}
