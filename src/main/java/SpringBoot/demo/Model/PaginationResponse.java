package SpringBoot.demo.Model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PaginationResponse<T> {
    private List<T> data;
    private PaginationInfo pagination;


    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PaginationInfo {
        private long totalItems;
        private int totalItemsPerPage;
        private int currentPage;
        private int totalPages;
        private int updateToday;

    }
}
