package com.theblood.springfood.actionlog.service.filter;

import lombok.*;
import tech.jhipster.service.filter.StringFilter;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ExtendedStringFilter extends StringFilter {
    private static final long serialVersionUID = 1L;
    private String startsWith;
    private String endsWith;

    public ExtendedStringFilter(ExtendedStringFilter filter) {
        super(filter);
        this.startsWith = filter.startsWith;
        this.endsWith = filter.endsWith;
    }

    @Override
    public ExtendedStringFilter copy() {
        return new ExtendedStringFilter(this);
    }
}
