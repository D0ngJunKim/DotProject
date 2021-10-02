package kdj.dotp.widget.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import kdj.dotp.DotFragment;
import kdj.dotp.R;
import kdj.dotp.widget.dao.FruitInfo;

/**
 * Created by 180842 on 2019-04-09.
 */
public class DotInnerAdapter extends RecyclerView.Adapter<DotInnerAdapter.InnerFruitViewHolder> {
    private final ArrayList<FruitInfo> mFruitList;
    private float mCurrentDiameter = DotFragment.MAX_DIAMETER;

    public DotInnerAdapter() {
        mFruitList = new ArrayList<>();
    }

    @NonNull
    @Override
    public InnerFruitViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        return new InnerFruitViewHolder(viewGroup);
    }

    @Override
    public void onBindViewHolder(@NonNull InnerFruitViewHolder holder, int pos) {
        holder.setData(mFruitList.get(pos), mCurrentDiameter);
    }

    public void setCurrentDiameter(float width) {
        mCurrentDiameter = width;
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

    static class InnerFruitViewHolder extends RecyclerView.ViewHolder {

        InnerFruitViewHolder(ViewGroup parent) {
            super(LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_dot_inner, parent, false));

            itemView.setOnClickListener(v -> {
                if (itemView.getY() + itemView.getHeight() / 2f >= itemView.getRootView().getHeight() / 2f) {
                    return;
                }
                Object tag = v.getTag();
                if (tag instanceof FruitInfo) {
                    Toast.makeText(v.getContext(), String.format("나는 %s 입니당!", ((FruitInfo) tag).getFruitNm()), Toast.LENGTH_SHORT).show();
                }
            });
        }

        public void setData(FruitInfo fruitInfo, float diameter) {
            itemView.setTag(fruitInfo);
            ((ImageView) itemView).setImageBitmap(getScaledBitmap(itemView.getContext(), fruitInfo.getFruitRes(), diameter));
        }


        private Bitmap getScaledBitmap(Context context, int res, float currentDiameter) {
            Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), res);
            return Bitmap.createScaledBitmap(bitmap, getResizedWidth(bitmap, currentDiameter), getResizedHeight(bitmap, currentDiameter), true);
        }

        private int getResizedWidth(Bitmap bitmap, float currentDiameter) {
            return (int) ((bitmap.getWidth() * currentDiameter) / DotFragment.MAX_DIAMETER);
        }

        private int getResizedHeight(Bitmap bitmap, float currentDiameter) {
            return (int) ((bitmap.getHeight() * currentDiameter) / DotFragment.MAX_DIAMETER);
        }
    }
}
