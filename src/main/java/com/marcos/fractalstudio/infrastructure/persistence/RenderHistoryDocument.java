package com.marcos.fractalstudio.infrastructure.persistence;

import java.util.List;

public record RenderHistoryDocument(List<RenderJobHistoryDocument> jobs) {
}
