package ayman.djangogenerator;

public class ItemModel {
    private String title;
    private int iconRes;
    private Class<?> destination;

    public ItemModel(String title, int iconRes, Class<?> destination) {
        this.title = title;
        this.iconRes = iconRes;
        this.destination = destination;
    }

    // Getters
    public String getTitle() { return title; }
    public int getIconRes() { return iconRes; }
    public Class<?> getDestination() { return destination; }
}
