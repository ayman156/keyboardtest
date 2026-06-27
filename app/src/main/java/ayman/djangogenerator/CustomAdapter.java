package ayman.djangogenerator;


import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class CustomAdapter extends RecyclerView.Adapter<CustomAdapter.ViewHolder> {
    private List<ItemModel> itemList;
    private Context context;

    public CustomAdapter(List<ItemModel> itemList, Context context) {
        this.itemList = itemList;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ItemModel item = itemList.get(position);
        holder.title.setText(item.getTitle());
        holder.icon.setImageResource(item.getIconRes());
        
        holder.itemView.setOnClickListener(v -> {
            if (item.getDestination() != null) {
                context.startActivity(new Intent(context, item.getDestination()));
            }
        });
    }

    @Override
    public int getItemCount() { return itemList.size(); }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView title;
        ImageView icon;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.itemTitle);
            icon = itemView.findViewById(R.id.itemIcon);
        }
    }
}
