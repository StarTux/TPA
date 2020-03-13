package com.winthier.tpa;

import java.util.UUID;
import lombok.Data;

@Data
public final class TPARequest {
    private final UUID sender;
    private final UUID target;
    private final long expiry;

    public TPARequest(final UUID sender, final UUID target, final long expiry) {
        this.sender = sender;
        this.target = target;
        this.expiry = expiry;
    }
}
