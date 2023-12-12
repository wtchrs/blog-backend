package xyz.firstlab.blog.dto;

import lombok.Getter;
import org.springframework.data.domain.Slice;

import java.util.List;

public class ResultList<T> {

    @Getter
    private final int count;

    private final boolean hasNext;

    @Getter
    private final List<T> results;

    public static <T> ResultList<T> from(Slice<T> slice) {
        return new ResultList<>(slice.hasNext(), slice.getContent());
    }

    public ResultList(boolean hasNext, List<T> results) {
        this.count = results.size();
        this.hasNext = hasNext;
        this.results = results;
    }

    public boolean hasNext() {
        return hasNext;
    }

}
