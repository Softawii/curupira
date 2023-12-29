package com.softawii.curupira.core;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.ItemComponent;
import net.dv8tion.jda.api.interactions.components.LayoutComponent;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.interactions.modals.Modal;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class CurupiraTests {

    static JDA jda;
    Curupira curupira;

    @BeforeAll
    static void initAll() {
        jda = JDABuilder.createDefault(System.getenv("discord_token")).build();
    }

    @BeforeEach
    void init() {
        jda.getRegisteredListeners()
                .stream()
                .filter(listener -> listener instanceof Curupira)
                .findFirst()
                .ifPresent(jda::removeEventListener);
        curupira = new Curupira(jda, true, null, "com.softawii.listener");
        jda.addEventListener(curupira);
    }

    @Test
    void validateInteractions() {
        assertEquals(6, curupira.getCommands().size());
    }

    @Test
    void validateHiddenGroup() {
        curupira.getCommands()
                .keySet()
                .forEach(key -> assertTrue(key.split(" ").length < 3));

        long commandsWithoutGroupCount = curupira.getCommands()
                .entrySet()
                .stream()
                .filter(entry -> {
                    String key = entry.getKey();
                    CommandHandler value = entry.getValue();
                    boolean hasAnyGroup = key.split(" ").length > 1;

                    return !value.isModal() && !hasAnyGroup;
                })
                .count();
        assertEquals(3, commandsWithoutGroupCount);
    }

    @Test
    void validateSubGroup() {
        long subgroupCommandsCount = curupira.getCommands()
                .keySet()
                .stream()
                .filter(key -> key.startsWith("bar-subgroup"))
                .count();

        assertEquals(2, subgroupCommandsCount);
    }

    @Test
    void validateModals() {
        Map<String, Modal> modals = curupira.getModals();
        Map<String, CommandHandler> commands = curupira.getCommands();

        assertEquals(1, modals.size());
        String key = modals.keySet().iterator().next();
        assertEquals(1, key.split(" ").length); // no group/subgroup
        assertEquals("support", key);

        CommandHandler commandHandler = commands.get(key);
        assertTrue(commandHandler.isModal());
        assertEquals(key, commandHandler.getName());
        assertEquals("Support Description", commandHandler.getDescription());

        Modal modal = modals.get(key);
        assertEquals("Support", modal.getTitle());
        assertEquals(key, modal.getId());

        // Modal components
        List<LayoutComponent> components = modal.getComponents();
        assertEquals(1, components.size());

        // Action row
        LayoutComponent layoutComponent = components.get(0);
        assertInstanceOf(ActionRow.class, layoutComponent);
        List<ItemComponent> actionRowComponents = layoutComponent.getComponents();

        // Text input
        assertEquals(1, actionRowComponents.size());
        assertInstanceOf(TextInput.class, actionRowComponents.get(0));
        TextInput textInput = (TextInput) actionRowComponents.get(0);
        assertEquals("name", textInput.getId());
        assertEquals("Name", textInput.getLabel());
        assertEquals("Enter your Name", textInput.getPlaceHolder());
        assertEquals(TextInputStyle.SHORT, textInput.getStyle());
        assertEquals(3, textInput.getMinLength());
        assertEquals(100, textInput.getMaxLength());
        assertTrue(textInput.isRequired());
        assertNull(textInput.getValue());
    }
}
