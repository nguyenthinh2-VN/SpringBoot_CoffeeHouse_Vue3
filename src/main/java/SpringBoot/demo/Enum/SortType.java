package SpringBoot.demo.Enum;

/**
 * Enum định nghĩa các loại sắp xếp sản phẩm
 * Tuân thủ nguyên lý Single Responsibility: chỉ quản lý các loại sắp xếp
 */
public enum SortType {
    POPULAR("popular", "Phổ biến"),
    NEWEST("newest", "Mới nhất"), 
    PRICE_ASC("price_asc", "Giá tăng dần"),
    PRICE_DESC("price_desc", "Giá giảm dần"),
    NAME_ASC("name_asc", "Tên A-Z"),
    NAME_DESC("name_desc", "Tên Z-A");

    private final String value;
    private final String displayName;

    SortType(String value, String displayName) {
        this.value = value;
        this.displayName = displayName;
    }

    public String getValue() {
        return value;
    }

    public String getDisplayName() {
        return displayName;
    }

    /**
     * Tìm SortType từ string value
     * @param value giá trị string
     * @return SortType tương ứng hoặc NEWEST nếu không tìm thấy
     */
    public static SortType fromValue(String value) {
        if (value == null) {
            return NEWEST;
        }
        
        for (SortType sortType : values()) {
            if (sortType.value.equalsIgnoreCase(value)) {
                return sortType;
            }
        }
        return NEWEST; // Default sort
    }
}
