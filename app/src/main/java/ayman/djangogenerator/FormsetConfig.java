package ayman.djangogenerator;

import org.json.JSONException;
import org.json.JSONObject;
import java.util.HashMap;

public class FormsetConfig {
    private long id;
    private long parentModelId;
    private long childModelId;
    private String relationshipName;
    private int extraFields;
    private boolean canDelete;
    private int maxNum;
    private String prefix;
    private String createdAt;
    
    // HashMap لتخزين المعلومات الإضافية
    private HashMap<String, String> additionalInfo;
    
    // Constructors
    public FormsetConfig() {
        this.extraFields = 1;
        this.canDelete = true;
        this.maxNum = 10;
        this.prefix = "formset";
        this.additionalInfo = new HashMap<>();
    }
    
    // Getters and Setters
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    
    public long getParentModelId() { return parentModelId; }
    public void setParentModelId(long parentModelId) { this.parentModelId = parentModelId; }
    
    public long getChildModelId() { return childModelId; }
    public void setChildModelId(long childModelId) { this.childModelId = childModelId; }
    
    public String getRelationshipName() { return relationshipName; }
    public void setRelationshipName(String relationshipName) { this.relationshipName = relationshipName; }
    
    public int getExtraFields() { return extraFields; }
    public void setExtraFields(int extraFields) { this.extraFields = extraFields; }
    
    public boolean isCanDelete() { return canDelete; }
    public void setCanDelete(boolean canDelete) { this.canDelete = canDelete; }
    
    public int getMaxNum() { return maxNum; }
    public void setMaxNum(int maxNum) { this.maxNum = maxNum; }
    
    public String getPrefix() { return prefix; }
    public void setPrefix(String prefix) { this.prefix = prefix; }
    
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    
    // Methods for additional info
    public void setAdditionalInfo(String key, String value) {
        this.additionalInfo.put(key, value);
    }
    
    public String getAdditionalInfo(String key) {
        return this.additionalInfo.get(key);
    }
    
    public HashMap<String, String> getAdditionalInfo() {
        return this.additionalInfo;
    }
    
    // إلى JSON
    public JSONObject toJson() throws JSONException {
        JSONObject json = new JSONObject();
        json.put("id", id);
        json.put("parent_model_id", parentModelId);
        json.put("child_model_id", childModelId);
        json.put("relationship_name", relationshipName);
        json.put("extra_fields", extraFields);
        json.put("can_delete", canDelete);
        json.put("max_num", maxNum);
        json.put("prefix", prefix);
        
        // إضافة المعلومات الإضافية
        for (String key : additionalInfo.keySet()) {
            json.put(key, additionalInfo.get(key));
        }
        
        return json;
    }
}