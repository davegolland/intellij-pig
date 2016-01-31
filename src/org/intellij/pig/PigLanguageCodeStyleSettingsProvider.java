package org.intellij.pig;

import com.intellij.lang.Language;
import com.intellij.psi.codeStyle.CodeStyleSettingsCustomizable;
import com.intellij.psi.codeStyle.LanguageCodeStyleSettingsProvider;
import org.jetbrains.annotations.NotNull;

public class PigLanguageCodeStyleSettingsProvider extends LanguageCodeStyleSettingsProvider {
    @NotNull
    @Override
    public Language getLanguage() {
        return PigLanguage.INSTANCE;
    }

    @Override
    public void customizeSettings(@NotNull CodeStyleSettingsCustomizable consumer, @NotNull SettingsType settingsType) {
        if (settingsType == SettingsType.SPACING_SETTINGS) {
            consumer.showStandardOptions("SPACE_AROUND_ASSIGNMENT_OPERATORS");
            consumer.renameStandardOption("SPACE_AROUND_ASSIGNMENT_OPERATORS", "Separator");
        } else if (settingsType == SettingsType.BLANK_LINES_SETTINGS) {
            consumer.showStandardOptions("KEEP_BLANK_LINES_IN_CODE");
        }
    }

    @Override
    public String getCodeSample(@NotNull SettingsType settingsType) {
        return "/**\n" +
                " * Sample Pig Script\n" +
                " */\n" +
                "set mapred.reduce.slowstart.completed.maps 0.999;\n" +
                "set job.name 'queue messages for sending';\n" +
                "\n" +
                "%declare today `date \"+%Y/%m/%d\"`\n" +
                "%declare kernel `uname -s`\n" +
                "%declare todayseconds `bash -c 'if [[ \"$kernel\" == \"Linux\" ]]; then date -d $today \"+%s\"; else date -jf \"%Y/%m/%d\" $today \"+%s\"; fi'`\n" +
                "%default todayUTCDate `date \\-\\-utc \"+%Y-%m-%d\"`\n" +
                "\n" +
                "/*\n" +
                "A job that computes things\n" +
                "*/\n" +
                "\n" +
                "REGISTER @fatjar@;\n" +
                "\n" +
                "inputData = LOAD '$input_data' USING BinaryJSON();\n" +
                "\n" +
                "emailSchedule = load '$email_schedule/#LATEST' using LiAvroStorage();\n" +
                "todaysSchedule = filter emailSchedule by receiveUTCDate == '$todayUTCDate';\n" +
                "\n" +
                "scheduledRecipients = filter todaysSchedule by campaign == '$email_key';\n" +
                "\n" +
                "outputData = join scheduledRecipients by memberId, inputData by recipientID PARALLEL 50;\n" +
                "\n" +
                "\n" +
                "-- reorder input to match output schema then send to production with Kafka writer\n" +
                "-- also rename template_id to emailKey\n" +
                "reordered = FOREACH outputData GENERATE\n" +
                "\t(long) $todayseconds * 1000 as expectedDeliveryDate:long,\n" +
                "\t(int) recipientID as recipientID:int,\n" +
                "\t-8 as gmtOffset:int,\n" +
                "\t(int) 2 as categoryID:int,\n" +
                "\t(int) 0 as priority:int,\n" +
                "\t'$email_key' as emailKey:chararray,\n" +
                "\tCONCAT('$email_key','$todayseconds') as contentID:chararray,\n" +
                "\tbody as body:chararray\n" +
                ";\n" +
                "\n" +
                "\n" +
                "\n" +
                "STORE reordered INTO '$";
    }
}