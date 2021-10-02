package kdj.dotp.widget.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.view.ViewKt;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import kdj.dotp.R;
import kdj.dotp.widget.dao.FruitInfo;
import kdj.dotp.widget.util.ExDimensionKt;

/**
 * Created by DJKim on 2019-06-20.
 */
public class DotOuterAdapter extends RecyclerView.Adapter<DotOuterAdapter.PagerViewHolder> {
    private final ArrayList<FruitInfo> mFruitList;

    public DotOuterAdapter() {
        mFruitList = new ArrayList<>();
    }

    @NonNull
    @Override
    public PagerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new PagerViewHolder(parent);
    }

    @Override
    public void onBindViewHolder(@NonNull PagerViewHolder holder, int position) {
        holder.setData(mFruitList.get(position));
    }

    @Override
    public int getItemCount() {
        return mFruitList.size();
    }

    public void setDataList(ArrayList<FruitInfo> fruitList) {
        mFruitList.clear();
        mFruitList.addAll(fruitList);
        notifyDataSetChanged();
    }


    static class PagerViewHolder extends RecyclerView.ViewHolder {
        private PagerItemViewHolder item01, item02, item03, item04, item05, item06;

        PagerViewHolder(ViewGroup parent) {
            super(LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_dot_outer, parent, false));

            item01 = new PagerItemViewHolder(itemView.findViewById(R.id.item_01));
            item02 = new PagerItemViewHolder(itemView.findViewById(R.id.item_02));
            item03 = new PagerItemViewHolder(itemView.findViewById(R.id.item_03));
            item04 = new PagerItemViewHolder(itemView.findViewById(R.id.item_04));
            item05 = new PagerItemViewHolder(itemView.findViewById(R.id.item_05));
            item06 = new PagerItemViewHolder(itemView.findViewById(R.id.item_06));
        }

        public void setData(FruitInfo fruitInfo) {
            item01.setData(fruitInfo);
            item02.setData(fruitInfo);
            item03.setData(fruitInfo);
            item04.setData(fruitInfo);
            item05.setData(fruitInfo);
            item06.setData(fruitInfo);
        }
    }

    static class PagerItemViewHolder extends RecyclerView.ViewHolder {
        private final ImageView ivImage;
        private final TextView tvTitle;

        PagerItemViewHolder(View rootView) {
            super(rootView);
            ivImage = rootView.findViewById(R.id.ivImage);
            tvTitle = rootView.findViewById(R.id.tvTitle);
        }

        public void setData(FruitInfo fruitInfo) {
            float widthRatio = 74f / 375f;
            int newCalcWidth = Math.round(ExDimensionKt.toPx(itemView.getContext().getResources().getConfiguration().screenWidthDp * widthRatio, itemView.getContext()));
            float heightRatio = 30f / 74f;
            int newCalcHeight = Math.round(newCalcWidth * heightRatio);

            int newWidth = Math.min(newCalcWidth, Math.round(ExDimensionKt.toPx(74, itemView.getContext())));
            int newHeight = Math.min(newCalcHeight, Math.round(ExDimensionKt.toPx(30, itemView.getContext())));

            if (ivImage.getLayoutParams().width != newWidth || ivImage.getLayoutParams().height != newHeight) {
                ViewKt.updateLayoutParams(ivImage, layoutParams -> {
                    layoutParams.width = newWidth;
                    layoutParams.height = newHeight;
                    return null;
                });
            }

            ivImage.setImageResource(fruitInfo.getFruitRes());
            tvTitle.setText(fruitInfo.getFruitNm());
        }
    }
}
