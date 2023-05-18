package ai.nextbillion.navigation.demo.adapter;

import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import ai.nextbillion.navigation.demo.R;
import ai.nextbillion.navigation.demo.model.Feature;
import ai.nextbillion.navigation.demo.utils.FontCache;
import androidx.recyclerview.widget.RecyclerView;

/**
 * Adapter used for FeatureOverviewActivity.
 * <p>
 * Adapts a Feature to a visual representation to be shown in a RecyclerView.
 * </p>
 */
public class FeatureAdapter extends RecyclerView.Adapter<FeatureAdapter.ViewHolder> {

    private final List<Feature> features;

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public TextView labelView;
        public TextView descriptionView;

        public ViewHolder(View view) {
            super(view);
            Typeface typeface = FontCache.get("Roboto-Regular.ttf", view.getContext());
            labelView = view.findViewById(R.id.nameView);
            labelView.setTypeface(typeface);
            descriptionView = view.findViewById(R.id.descriptionView);
            descriptionView.setTypeface(typeface);
        }
    }

    public FeatureAdapter(List<Feature> features) {
        this.features = features;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_main_feature, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.labelView.setText(features.get(position).getLabel());
        holder.descriptionView.setText(features.get(position).getDescription());
    }

    @Override
    public int getItemCount() {
        return features.size();
    }
}
