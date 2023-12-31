package com.kodo.embeds;

import java.awt.Color;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.annotation.Nonnull;

import com.kodo.codewars.scraper.CodewarsKata;
import com.kodo.utils.QuoteUtils;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.components.ItemComponent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;

public class EmbedMaker {

    private static final String codeLine = "```";
    private static ExecutorService executorService = Executors.newFixedThreadPool(3);

    @Nonnull
    public static EmbedBuilder ERROR(User profile, String notice, String error_type) {
        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setTitle("Error Notice");
        embedBuilder.setDescription(notice);
        embedBuilder.setColor(Color.red);

        String localTime = LocalDateTime.now().toString();

        if (error_type != null && !localTime.isEmpty()) {
            embedBuilder.addField("Response", codeLine + error_type + codeLine, false);
        }

        embedBuilder.setFooter("User: " + profile.getName() + " | ID: " + profile.getId(), profile.getAvatarUrl());
        embedBuilder.setTimestamp(Instant.now());    

        return embedBuilder;
    }

        @Nonnull
    public static EmbedBuilder ERROR_BASIC(User profile) {
        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setTitle("Error Notice");
        embedBuilder.setColor(Color.red);
        embedBuilder.setFooter("User: " + profile.getName() + " | ID: " + profile.getId(), profile.getAvatarUrl());
        embedBuilder.setTimestamp(Instant.now());    
        return embedBuilder;
    }

    @Nonnull
    public static EmbedBuilder INFO(User user, String notice) {
        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setTitle("INFO");
        embedBuilder.setDescription(notice);
        embedBuilder.setColor(Color.green);
        embedBuilder.setTimestamp(Instant.now());
        embedBuilder.setFooter("User: " + user.getName() + " | ID: " + user.getId(), user.getAvatarUrl());

        return embedBuilder;
    }

    @SuppressWarnings("null")
    public static CodewarsChallengeEmbed getKataEmbed(CodewarsKata challenge){

        EmbedBuilder page1 = new EmbedBuilder();
        page1.setColor(challenge.getRank().getColorEnum());
        page1.setTitle(challenge.getName(), challenge.getUrl());

        String description = challenge.getDescription();

        if(description.length() > 4000){
            description = description.substring(0, 4000) + "...";
        }

        page1.setDescription(description);

        EmbedBuilder page2 = new EmbedBuilder();

        page2.setColor(challenge.getRank().getColorEnum());
        page2.setTitle(challenge.getName(), challenge.getUrl());

        page2.addField("Tags", "```" + Arrays.toString(challenge.getTags().toArray()) + "```", false);
        page2.addField("Rank", "```" + challenge.getRank().getName() + "```", false);
        page2.addField("Category", "```" + challenge.getCategory() + "```", false);
        page2.addField("Languages", "```" + Arrays.toString(challenge.getLanguages().toArray()) + "```", false);
        page2.addField("Stars", "```" + challenge.getTotalStars() + "```", true);
        page2.addField("Completed", "```" + challenge.getTotalCompleted() + "```", true);
        page2.addField("Approved At", "```" + challenge.getApprovedAt() + "```", true);

        if(challenge.getApprovedBy() != null){
            page2.addField("Approved By",  "```" + challenge.getApprovedBy().getUsername()  + "```", true);
        }

        page2.addField("Author", "```" + challenge.getCreatedBy().getUsername()  + "```", true);
        page2.addField("Total Attempts", "```" + challenge.getTotalAttempts() + "```", true);
        page2.addField("Vote score", "```" + challenge.getVoteScore() + "```", true); 

        PagedEmbed builder = new PagedEmbed();
        builder.appendPages(page1, page2);

        Button button1 = Button.primary("codewars_profile", "View Kata")
            .withUrl(challenge.getUrl())
            .withStyle(ButtonStyle.LINK);

        List<Button> buttons = new ArrayList<>();
        buttons.add(button1);

        CodewarsChallengeEmbed challengeEmbed = new CodewarsChallengeEmbed(builder, buttons);

        return challengeEmbed;
    }

    /**
     * This method returns a loading embed, meanwhile running the future task, be sure to edit the original message with the result
     * @param event
     * @param future
     * @return
     */
    public static void runAsyncTask(SlashCommandInteractionEvent event, Runnable runnable) {
        EmbedBuilder loadingEmbedBuilder = new EmbedBuilder();
        loadingEmbedBuilder.setTitle("Loading task...");
        loadingEmbedBuilder.setDescription("Hang tight while this task, this may take some time depending on the request. In the meantime, here are some quotes to keep you entertained.");
        loadingEmbedBuilder.setColor(Color.green);
        loadingEmbedBuilder.setTimestamp(Instant.now());

        loadingEmbedBuilder.setImage("https://media.tenor.com/RVvnVPK-6dcAAAAC/reload-cat.gif");

        loadingEmbedBuilder.setFooter("User: " + event.getUser().getName() + " | ID: " + event.getUser().getId(), event.getUser().getAvatarUrl());
        loadingEmbedBuilder.setTimestamp(Instant.now());
        loadingEmbedBuilder.addField("Quote", "```" + QuoteUtils.getRandomQuote() + "```", true);

        MessageEmbed loadedEmbed = loadingEmbedBuilder.build();

        //Send the loading embed
        event.replyEmbeds(loadedEmbed).queue();
        
        executorService.submit(() -> {
            try {
                runnable.run();
            } catch (Exception e) {
                EmbedBuilder builder = EmbedMaker.ERROR(event.getUser(), "Whoops, an error has occured.", e.getLocalizedMessage());
                event.getHook().editOriginalEmbeds(builder.build()).queue();
                e.printStackTrace();
                e.printStackTrace();
            }
        });
    }

    /**
     * This method returns a loading embed, meanwhile running the future task, be sure to edit the original message with the result
     * @param event
     * @param future
     * @return
     */
    public static void sendAsyncMessage(SlashCommandInteractionEvent event, EmbedBuilderSupplier<EmbedBuilder> task) {
        EmbedMaker.runAsyncTask(event, () -> {
            try{
                EmbedBuilder embed = task.call(event.getHook());
                event.getHook().editOriginalEmbeds(embed.build()).queue();
            }catch(Exception e){
                EmbedBuilder builder = EmbedMaker.ERROR(event.getUser(), "Whoops, an error has occured.", e.getLocalizedMessage());
                event.getHook().editOriginalEmbeds(builder.build()).queue();
                e.printStackTrace();
            }
        });
    }

    /**
     * This method returns a loading embed, meanwhile running the future task, be sure to edit the original message with the result
     * @param event
     * @param future
     * @return
     */
    @SuppressWarnings("null")
    public static void sendAsyncMessageWithActionItems(SlashCommandInteractionEvent event, EmbedBuilderSupplier<EmbedBuilder> task, ItemComponent... items) {
        EmbedMaker.runAsyncTask(event, () -> {
            try{
                EmbedBuilder embed = task.call(event.getHook());
                event.getHook().editOriginalEmbeds(embed.build()).setActionRow(items).queue();
            }catch(Exception e){
                EmbedBuilder builder = EmbedMaker.ERROR(event.getUser(), "Whoops, an error has occured.", e.getLocalizedMessage());
                event.getHook().editOriginalEmbeds(builder.build()).queue();
                e.printStackTrace();
            }
        });
    }

    /**
     * This interface is used to pass the hook to the future
     * @param <T>
     */
    @FunctionalInterface
    public interface EmbedBuilderSupplier <T> {
        T call(InteractionHook hook);
    }    
}
