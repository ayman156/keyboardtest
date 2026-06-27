// TemplatesAdapter.java - النسخة المعدلة
package ayman.djangogenerator;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class TemplatesAdapter extends RecyclerView.Adapter<TemplatesAdapter.ViewHolder> {
    
    private Context context;
    private List<Template> templates;  // استخدام الفئة المنفصلة
    private OnTemplateClickListener listener;
    
    public interface OnTemplateClickListener {
        void onTemplateClick(Template template);
    }
    
    public TemplatesAdapter(Context context, List<Template> templates) {
        this.context = context;
        this.templates = templates;
    }
    
    public void setOnTemplateClickListener(OnTemplateClickListener listener) {
        this.listener = listener;
    }
    
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
            .inflate(android.R.layout.simple_list_item_2, parent, false);
        return new ViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Template template = templates.get(position);
        
        holder.text1.setText(template.getName());
        String description = template.getDescription();
        holder.text2.setText(description != null && !description.isEmpty() ? 
            description : "لا يوجد وصف");
        
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onTemplateClick(template);
            }
        });
    }
    
    @Override
    public int getItemCount() {
        return templates == null ? 0 : templates.size();
    }
    
    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView text1;
        TextView text2;
        
        public ViewHolder(View itemView) {
            super(itemView);
            text1 = itemView.findViewById(android.R.id.text1);
            text2 = itemView.findViewById(android.R.id.text2);
        }
    }
}