package ayman.djangogenerator;

import org.json.JSONObject;
import org.json.JSONException;

public class Field {
    // خصائص قاعدة البيانات
    private long id;
    private long modelId;
    private String name;
    private String type;
    private JSONObject options = new JSONObject();
    private int order;
    
    // خصائص التقارير
    private String displayName = "";
    private boolean includeInReport = true;
    private boolean groupBy = false;
    private boolean orderBy = false;
    private String orderDirection = "ASC";
    //private String orderDirection = "ASC";
    private String format; // إضافة هذا
    private String condition; // إضافة هذا
    
    
    // Constructors
    public Field() {
        try {
            options.put("max_length", 255);
            options.put("blank", false);
            options.put("null", false);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    
    public Field(long id, String name, String type) {
        this();
        this.id = id;
        this.name = name;
        this.type = type;
        this.displayName = name;
    }
    
    
        // Getters and Setters
    public String getFormat() { return format; }
    public void setFormat(String format) { this.format = format; }
    
    public String getCondition() { return condition; }
    public void setCondition(String condition) { this.condition = condition; }
    
    public void reset() {
        this.displayName = this.name;
        this.includeInReport = false;
        this.groupBy = false;
        this.orderBy = false;
        this.orderDirection = "ASC";
        this.format = null;
        this.condition = null;
    }
    // Getters for database
    public long getId() { return id; }
    public long getModelId() { return modelId; }
    public String getName() { return name; }
    public String getType() { return type; }
    public JSONObject getOptions() { return options; }
    public int getOrder() { return order; }
    
    // Getters for reports
    public String getDisplayName() { 
        return displayName.isEmpty() ? name : displayName; 
    }
    public boolean isIncludeInReport() { return includeInReport; }
    public boolean isGroupBy() { return groupBy; }
    public boolean isOrderBy() { return orderBy; }
    public String getOrderDirection() { return orderDirection; }
    
    // Setters for database
    public void setId(long id) { this.id = id; }
    public void setModelId(long modelId) { this.modelId = modelId; }
    public void setName(String name) { this.name = name; }
    public void setType(String type) { this.type = type; }
    public void setOptions(JSONObject options) { 
        if (options != null) this.options = options; 
    }
    public void setOrder(int order) { this.order = order; }
    
    // Setters for reports
    public void setDisplayName(String displayName) { this.displayName = displayName; }
    public void setIncludeInReport(boolean includeInReport) { 
        this.includeInReport = includeInReport; 
    }
    public void setGroupBy(boolean groupBy) { this.groupBy = groupBy; }
    public void setOrderBy(boolean orderBy) { this.orderBy = orderBy; }
    public void setOrderDirection(String orderDirection) { 
        this.orderDirection = orderDirection; 
    }
    
    // Helper methods
    public String getFieldDefinition() {
        StringBuilder sb = new StringBuilder();
        sb.append(name).append(" = models.").append(type).append("(");
        
        try {
            if (options.has("verbose_name")) {
                sb.append("verbose_name=\"").append(options.getString("verbose_name")).append("\", ");
            }
            
            if (type.equals("CharField") && options.has("max_length")) {
                sb.append("max_length=").append(options.getInt("max_length")).append(", ");
            }
            
            if (options.has("blank") && options.getBoolean("blank")) {
                sb.append("blank=True, ");
            }
            
            if (options.has("null") && options.getBoolean("null")) {
                sb.append("null=True, ");
            }
            
            // إزالة الفاصلة الأخيرة إذا وجدت
            if (sb.toString().endsWith(", ")) {
                sb.setLength(sb.length() - 2);
            }
            
        } catch (JSONException e) {
            e.printStackTrace();
        }
        
        sb.append(")");
        return sb.toString();
    }
}
