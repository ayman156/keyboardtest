package ayman.djangogenerator;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class FieldsAdapter extends RecyclerView.Adapter<FieldsAdapter.ViewHolder> {
    
    private Context context;
    private List<Field> fieldList;
    private OnFieldClickListener listener;
    
    public interface OnFieldClickListener {
        void onFieldClick(int position);
    }
    
    public void setOnFieldClickListener(OnFieldClickListener listener) {
        this.listener = listener;
    }
    
    public FieldsAdapter(Context context, List<Field> fieldList) {
        this.context = context;
        this.fieldList = fieldList;
    }
    
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_field, parent, false);
        return new ViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Field field = fieldList.get(position);
        
        holder.tvFieldName.setText(field.getName());
        holder.tvFieldType.setText("النوع: " + field.getType());
        
        holder.cbInclude.setChecked(field.isIncludeInReport());
        holder.cbInclude.setOnCheckedChangeListener((buttonView, isChecked) -> {
            field.setIncludeInReport(isChecked);
        });
        
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onFieldClick(position);
            }
        });
    }
    
    @Override
    public int getItemCount() {
        return fieldList.size();
    }
    
    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvFieldName, tvFieldType;
        CheckBox cbInclude;
        
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvFieldName = itemView.findViewById(R.id.tvFieldName);
            tvFieldType = itemView.findViewById(R.id.tvFieldType);
            cbInclude = itemView.findViewById(R.id.cbInclude);
        }
    }
}