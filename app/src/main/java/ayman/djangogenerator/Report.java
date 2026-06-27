// Report.java
package ayman.djangogenerator;

public class Report {
    private long id;
    private long projectId;
    private String name;
    private String description;
    private long modelId;
    private String modelName;
    private String configJson;
    private String djangoQuery;
    private long createdAt;
    
    // Constructors
    public Report() {}
    
    public Report(long id, long projectId, String name, String description, 
                  long modelId, String modelName, String configJson, 
                  String djangoQuery, long createdAt) {
        this.id = id;
        this.projectId = projectId;
        this.name = name;
        this.description = description;
        this.modelId = modelId;
        this.modelName = modelName;
        this.configJson = configJson;
        this.djangoQuery = djangoQuery;
        this.createdAt = createdAt;
    }
    
    // Getters and Setters
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    
    public long getProjectId() { return projectId; }
    public void setProjectId(long projectId) { this.projectId = projectId; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public long getModelId() { return modelId; }
    public void setModelId(long modelId) { this.modelId = modelId; }
    
    public String getModelName() { return modelName; }
    public void setModelName(String modelName) { this.modelName = modelName; }
    
    public String getConfigJson() { return configJson; }
    public void setConfigJson(String configJson) { this.configJson = configJson; }
    
    public String getDjangoQuery() { return djangoQuery; }
    public void setDjangoQuery(String djangoQuery) { this.djangoQuery = djangoQuery; }
    
    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
}