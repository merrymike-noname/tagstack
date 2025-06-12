package org.kovalenko.tagstack.entity;

import java.util.List;

public interface TagComponent {
    Integer getId();
    String getName();
    List<? extends TagComponent> getChildren();
    boolean hasChildren();
    Integer getUserId();
}
