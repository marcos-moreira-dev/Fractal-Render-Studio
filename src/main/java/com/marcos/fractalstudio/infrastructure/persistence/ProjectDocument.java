package com.marcos.fractalstudio.infrastructure.persistence;

import java.util.List;

public record ProjectDocument(
        String id,
        String name,
        String fractalFormulaType,
        RenderProfileDocument renderProfile,
        ColorProfileDocument colorProfile,
        ProjectMetadataDocument metadata,
        ProjectSettingsDocument settings,
        List<KeyframeDocument> keyframes,
        List<BookmarkDocument> bookmarks
) {
}
