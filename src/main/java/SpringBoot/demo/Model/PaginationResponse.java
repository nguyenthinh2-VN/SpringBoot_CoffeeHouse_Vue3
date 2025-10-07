package SpringBoot.demo.Model;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class PaginationResponse<T> {
    private List<T> data;
    private PaginationInfo pagination;

    public PaginationResponse() {
    }

    public PaginationResponse(List<T> data, PaginationInfo pagination) {
        this.data = data;
        this.pagination = pagination;
    }

    @Getter
    @Setter
    public static class PaginationInfo {
        private long totalItems;
        private int totalItemsPerPage;
        private int currentPage;
        private int totalPages;
        private int updateToday;

        public PaginationInfo() {
        }

        public PaginationInfo(long totalItems, int totalItemsPerPage, int currentPage, int totalPages, int updateToday) {
            this.totalItems = totalItems;
            this.totalItemsPerPage = totalItemsPerPage;
            this.currentPage = currentPage;
            this.totalPages = totalPages;
            this.updateToday = updateToday;
        }
    }
}
