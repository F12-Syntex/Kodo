package com.kodo.embeds;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.annotation.Nonnull;

import com.kodo.bot.Kodo;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.EventListener;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.components.buttons.Button;


//TODO: remove paged embeds after certain duration
public class PagedEmbed extends EmbedBuilder implements EventListener {

    private int page = 0;
    private List<EmbedBuilder> pages = new ArrayList<>();

    private String nextId = UUID.randomUUID().toString();
    private String previousId = UUID.randomUUID().toString();

    private InteractionHook hook;

    public void appendPage(EmbedBuilder embed){
        this.pages.add(embed);
    }

    public void appendPages(EmbedBuilder... embed){
        for(EmbedBuilder i : embed){
            this.pages.add(i);
        }
    }

    @SuppressWarnings("null")
    public void send(SlashCommandInteractionEvent event){
        this.hook = event.getHook();
        event.replyEmbeds(this.build()).addActionRow(this.getButtons()).queue();
    }

    @SuppressWarnings("null")
    public void sendEdit(InteractionHook event){
        this.hook = event;
        event.editOriginalEmbeds(this.build()).setActionRow(this.getButtons()).queue();
    }

    public void sendEdit(InteractionHook event, List<Button> buttons){
        this.hook = event;
        buttons.addAll(this.getButtons());
        event.editOriginalEmbeds(this.build()).setActionRow(buttons).queue();
    }

    @SuppressWarnings("null")
    @Override
    public void onEvent(@Nonnull GenericEvent event){
        if (event instanceof ButtonInteractionEvent) {
            ButtonInteractionEvent messageEvent = (ButtonInteractionEvent) event;
            Button button = messageEvent.getButton();        
            
            String btnId = button.getId();
            
            if(btnId == null) return;


            if(btnId.equals(nextId)){
                this.nextPage();
                List<Button> buttons = this.getButtons();
                this.hook.editOriginalEmbeds(this.build()).setActionRow(buttons).queue();
                messageEvent.deferEdit().queue();
            }

            if(btnId.equals(previousId)){
                this.previousPage();
                List<Button> buttons = this.getButtons();
                this.hook.editOriginalEmbeds(this.build()).setActionRow(buttons).queue();
                 messageEvent.deferEdit().queue();
            }

        }
    }

    @SuppressWarnings("null")
    public List<Button> getButtons() {
        List<Button> buttons = new ArrayList<>();

        if (this.hasPreviousPage()) {
            Button previousButton = Button.primary(previousId, "\u25C0 Previous");
            buttons.add(previousButton);
        } else {
            Button previousButtonDisabled = Button.secondary(previousId, "\u25C0 Previous").asDisabled();
            buttons.add(previousButtonDisabled);
        }

        if (this.hasNextPage()) {
            Button nextButton = Button.primary(nextId, "Next \u25B6");
            buttons.add(nextButton);
        } else {
            Button nextButtonDisabled = Button.secondary(nextId, "Next \u25B6").asDisabled();
            buttons.add(nextButtonDisabled);
        }

        return buttons;
    }


    @Nonnull
    @Override
    public MessageEmbed build(){
        //print the class, line number and method that called this function
        System.out.println("page: " + this.page + " pages: " + this.pages.size());
        return pages.get(page).setFooter("Showing page " + (this.page+1) + "/" + this.pages.size()).build();
    }
    
    /**
     * 
     */
    @SuppressWarnings("deprecation")
    public PagedEmbed(){
        super();
        Kodo.getInstance().getDiscord().addEventListener(this);

        //automatically kill this listener after 5 minutes
        // new java.util.Timer().schedule(
        //     new java.util.TimerTask() {
        //         @Override
        //         public void run() {
        //             Kodo.getInstance().getDiscord().removeEventListener(PagedEmbed.this);
        //         }
        //     },
        //     1000 * 60 * 5
        // );

    }

    public void setPage(int page){
        this.page = page;
    }

    public void nextPage(){
        if(this.hasNextPage()){
            this.page++;
        }
    }

    public void previousPage(){
        if(this.hasPreviousPage()){
            this.page--;
        }
    }

    public boolean hasNextPage(){
        return page + 1 < pages.size();
    }

    public boolean hasPreviousPage(){
        return this.page > 0;
    }

    public int getPage() {
        return page;
    }
}
