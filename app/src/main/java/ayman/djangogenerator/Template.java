// Template.java
package ayman.djangogenerator;

public class Template {
    private long id;
    private String name;
    private String description;
    private String htmlContent;
    private String cssContent;
    private String jsContent;
    private boolean isSystem;
    private long projectId;
    
    public Template() {
        htmlContent = "";
        cssContent = "";
        jsContent = "";
        isSystem = false;
    }
    
    public Template(long id, String name, String description, String htmlContent, 
                   String cssContent, String jsContent, boolean isSystem, long projectId) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.htmlContent = htmlContent;
        this.cssContent = cssContent;
        this.jsContent = jsContent;
        this.isSystem = isSystem;
        this.projectId = projectId;
    }
    
    // Getters and Setters
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public String getHtmlContent() { return htmlContent; }
    public void setHtmlContent(String htmlContent) { this.htmlContent = htmlContent; }
    
    public String getCssContent() { return cssContent; }
    public void setCssContent(String cssContent) { this.cssContent = cssContent; }
    
    public String getJsContent() { return jsContent; }
    public void setJsContent(String jsContent) { this.jsContent = jsContent; }
    
    public boolean isSystem() { return isSystem; }
    public void setSystem(boolean system) { isSystem = system; }
    
    public long getProjectId() { return projectId; }
    public void setProjectId(long projectId) { this.projectId = projectId; }
}