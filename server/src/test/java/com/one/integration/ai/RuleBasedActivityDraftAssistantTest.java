package com.one.integration.ai;

import com.one.activity.ActivityMode;
import com.one.activity.ActivityType;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RuleBasedActivityDraftAssistantTest {

    private final RuleBasedActivityDraftAssistant assistant = new RuleBasedActivityDraftAssistant();

    @Test
    void shouldExtractCoreFieldsFromNaturalLanguage() {
        ActivityDraftAssistant.DraftSuggestion result = assistant.suggest(
                "周六下午在静安打羽毛球，缺三个人，新手，不卷，鸽子金20元");

        assertThat(result.type()).isEqualTo(ActivityType.BADMINTON);
        assertThat(result.mode()).isEqualTo(ActivityMode.OFFLINE);
        assertThat(result.capacity()).isEqualTo(3);
        assertThat(result.depositFen()).isEqualTo(2_000);
        assertThat(result.tags()).contains("新手友好", "轻松局");
        assertThat(result.fieldsRequiringConfirmation()).contains("startAt", "address");
    }

    @Test
    void shouldParseTwoDigitChineseCapacityAndClampToProductLimit() {
        ActivityDraftAssistant.DraftSuggestion result = assistant.suggest("线上游戏局，限二十一人，可以开麦");

        assertThat(result.type()).isEqualTo(ActivityType.GAMING);
        assertThat(result.mode()).isEqualTo(ActivityMode.ONLINE);
        assertThat(result.capacity()).isEqualTo(20);
        assertThat(result.attributes()).containsEntry("mic", "可以开麦");
    }
}
