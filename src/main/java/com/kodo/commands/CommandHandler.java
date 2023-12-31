package com.kodo.commands;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import javax.annotation.Nonnull;

import org.reflections.Reflections;

import com.kodo.handler.Dependencies;
import com.kodo.handler.Handler;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
public class CommandHandler extends Handler{
    
    private final Set<Command> commands;
    private final Logger logger = Logger.getGlobal();
    private final Dependencies dependencies;


    public CommandHandler(Dependencies dependencies) {
        //Scan all the classes in this package for commands, then dynamically load them
        this.commands = new HashSet<>();
        this.dependencies = dependencies;
    }

    public void loadCommands() {
       
        //scan all classes in this package for commands
        Reflections reflections = new Reflections(this.getClass().getPackage().getName());

        //log the package we are scanning
        logger.info("Scanning for commands in package: " + this.getClass().getPackage().getName());

        //get all classes that extend Command
        Set<Class<? extends Command>> clazzes = reflections.getSubTypesOf(Command.class);

        //for every class ( which is a command ) add it to our commands set
        clazzes.forEach(commandClazz -> {
            
            if(commandClazz.isInterface() || Modifier.isAbstract(commandClazz.getModifiers())) return;

            try {
                
                Command command = commandClazz.getConstructor(Dependencies.class).newInstance(this.dependencies);

                CommandMeta meta = command.getMetaInformation();
                logger.info("Command loaded: " + meta.name());
                
                this.commands.add(command);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        logger.info("Loaded " + this.commands.size() + " command(s)");
    }

    public void registerCommands() {
        JDA jda = this.dependencies.getDiscord();

        List<SlashCommandData> slashCommands = new ArrayList<>();

        this.commands.forEach(command -> {
            logger.info("Registering command: " + command.getMetaInformation().name());
            SlashCommandData data = command.getSlashCommandData();
            if (data != null) {
                slashCommands.add(data);
            } else {
                logger.warning("Command " + command.getMetaInformation().name() + " has no slash command data");
            }
        });

        jda.updateCommands().addCommands(slashCommands).queue();
    }


    @Override
    public void onSlashCommandInteraction(@Nonnull SlashCommandInteractionEvent event) {
        String name = event.getName();

        //for every command, check if the name matches the name of the command, if so, run the command
        this.commands.stream().filter((i) -> i.getMetaInformation().name().equals(name)).forEach((i) -> i.handleInteraction(event));
    }

    /**
     * @param <T> Instance of command
     * @param clazz the class of the command to get
     * @return
     */
    @SuppressWarnings("unchecked")
    public <T extends Command> T getCommand(Class<? extends Command> clazz){
        return (T) this.commands.stream().filter((i) -> i.getClass().equals(clazz)).findFirst().orElse(null);
    }
    
}
    

