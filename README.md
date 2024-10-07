<div align="center">

# Curupira
[![Java CI with Gradle](https://github.com/Softawii/curupira/actions/workflows/gradle.yml/badge.svg)](https://github.com/Softawii/curupira/actions/workflows/gradle.yml)
[![](https://jitpack.io/v/Softawii/curupira.svg)](https://jitpack.io/#Softawii/curupira)

</div>

### Install with Gradle

```
repositories {
    maven { url 'https://jitpack.io/' }
}
    
dependencies { 
    implementation("com.github.Softawii:curupira:VERSION:all")
}
```

### Getting Started

Curupira is a framework built with [JDA](https://github.com/discord-jda/JDA) that simplifies 
the integration of Discord commands into your bot. It uses Java's annotation system to 
automatically map commands and parameters to your bot's code, making development 
faster and more efficient.

Key features include:

- **Support for Slash, User and Message Command Mapping**
- **Automatic Command Linking with Discord** (Slash, User, and Message Commands)
- **Slash Command Parameters Declared as Function Parameters**
- **Support for Menus, Modals, and Selects Mapping**
- **Auto-Complete Functionality**
- **Auto-Translation Support**
- **Built-in Exception Handling**

### Building Your First Application

#### Start-up

Curupira provides a simple constructor for initializing the framework. See the example below:

```java
public CurupiraBoot(JDA jda, ContextProvider context, boolean registerCommandsToDiscord, String... packages);
```

- **JDA**: The JDA instance that will handle linking commands and receiving events.
- **ContextProvider**: A mechanism to retrieve instances of the required classes.
- **registerCommandsToDiscord**: When debugging, you can skip command registration on every startup.
- **packages**: The packages to scan for annotations.

The `ContextProvider` can be customized. In the built-in implementation, we provide a `BasicContextProvider`, 
but you can easily integrate it with more complex frameworks like Spring Boot.

Here’s a simple example of the `BasicContextProvider`:

```java
BasicContextProvider context = new BasicContextProvider();

context.registerInstance(GenericExceptionHandler.class, new GenericExceptionHandler());
context.registerInstance(BasicController.class, new BasicController());
context.registerInstance(ComplexController.class, new ComplexController());
context.registerInstance(TranslatedController.class, new TranslatedController());
context.registerInstance(AutoMenuController.class, new AutoMenuController());
```

For a more flexible approach, you can integrate with Spring Boot using a custom `SpringContextProvider`:

```java
@Component
public class SpringContextProvider implements ContextProvider {

    private final ApplicationContext context;

    public SpringContextProvider(ApplicationContext context) {
        this.context = context;
    }

    @Override
    public <T> T getInstance(Class<T> aClass) {
        return context.getBean(aClass);
    }
}
```

#### Creating a Controller

To create a controller you need to add a `@DiscordController` annotation to your class.  
The `@DiscordController` annotation is used to define and configure a class as a controller for handling
Discord commands and interactions. It allows the scan of commands, modals, and buttons to this class.  
Let's check the parameters:

##### General

- `value`
  - **type**: `String`
  - **brief**: The name or identifier of the controller.
  - **example**: /value command

- `parent` (Optional)
  - **type**: `String`
  - **default**: <empty>
  - **brief**: Specifies a parent controller name, allowing for hierarchical command structures.  
    If left empty, the controller is considered a root-level command.
  - **example**: /parent value command

- `description`
  - **type**: `String`
  - **brief**: A brief description of the controller, which can be displayed in help menus or documentation.

- `hidden` (Optional)
  - **type**: `Boolean`
  - **default**: false
  - **brief**: Indicates whether the controller's prefix will be hidden.
  - **example**: /~~value~~ command

- `permissions` (Optional)
  - **type**: `Permission[]`
  - **default**: <empty>
  - **brief**: Specifies the required permissions for executing commands under this controller.  
    If empty, no specific permissions are required. Controllers with the same parent must have the same permission array.

- `environment` (Optional)
  - **type**: `DiscordEnvironment`
  - **default**: `DiscordEnvironment.SERVER`
  - **brief**: Defines the environment in which the controller is active.

##### Internationalization (I18n)

- `resource` (Optional)
  - **type**: `String`
  - **brief**: Specifies the resource file for localizing the controller and its commands.  
    This is useful for supporting multiple languages in your bot.

- `locales` (Optional)
  - **type**: `DiscordLocale[]`
  - **default**: false
  - **brief**: An array of supported locales for the controller. A resource file must be provided.

- `defaultLocale` (Optional)
  - **type**: `DiscordLocale`
  - **default**: `DiscordLocale.ENGLISH_US`
  - **brief**: The default locale to be used when no specific locale is a match to the user.

##### Examples

```java
@DiscordController(value = "basic", description = "Basic Controller")
public class BasicController {
    
}

@DiscordController(parent = "social", value = "twitter", description = "Twitter Controller", permissions = Permission.ADMINISTRATOR,
        resource = "social", locales = DiscordLocale.PORTUGUESE_BRAZILIAN)
public class SocialTwitterGroup {

}

@DiscordController(value = "agent", description = "Voice Agent Controller", resource = "voice", locales = DiscordLocale.PORTUGUESE_BRAZILIAN)
public class VoiceAgentController {
    
}
```

#### Creating a Command

Commands are defined using the `@DiscordCommand` annotation, which maps each method to a Discord interaction. 
The basic parameters you need to specify are the name and description of the command. For additional customization, 
you can use parameters like ephemeral (to make responses visible only to the user) and type (to define the command type).

Curupira simplifies your development by automatically injecting relevant information through special annotations:

- `@RequestInfo`: Injects context-specific information such as the User, Member, or Channel.
- `@LocaleType`: Provides information about the User or Server Locale.
Here's a simple example:

```java
@DiscordCommand(name = "hello", description = "Hello World", ephemeral = true)
public String hello(@RequestInfo Member member) {
    return "Hello World, " + member.getNickname() + "!";
}
```

In this example:

- The command `/hello` will greet the user by their nickname.
- The response is ephemeral, meaning only the user who invoked the command will see the message.

You can integrate access to a `Guild` directly by including it as a method parameter. 
No additional annotation is needed—just declare a parameter of type Guild, 
and Curupira will automatically inject the relevant instance.

```java
@DiscordCommand(name = "server", description = "Get server information")
public String getServerInfo(Guild guild) {
    return "This command was run in: " + guild.getName();
}
```


If you need more control over the response or wish to directly interact with the event, 
you can retrieve the event object itself. This allows you to handle replies, embeds, 
and other advanced interactions directly.

```java
@DiscordCommand(name = "response", description = "Response")
public void response(SlashCommandInteractionEvent event) {
    MessageEmbed embed = new EmbedBuilder().setTitle("Hello World!").build();
    event.reply("Hello World!").setEphemeral(true).addEmbeds(embed).setEphemeral(true).queue();
}
```

In this example:

- The `/response` command replies with a simple message and an embedded message titled "Hello World!"
- The SlashCommandInteractionEvent object gives you full control over the reply, allowing you to customize 
how the bot responds to the user. You can set the reply as ephemeral, attach embeds, and more.

You can specify parameters too. Parameters for commands can be defined using the `@DiscordParameter` annotation.  
You need to specify a name and description.  
Additionally, you can specify if the parameter is required and even provide autocomplete options  using the `choices` field or a fully-dedicated function by setting the `autoComplete` field (which will be explained below).

Here's a simple example:

```java
@DiscordCommand(name = "greetings", description = "greetings")
public String greetings(
    @RequestInfo Member member,
    @DiscordParameter(name = "name", description = "Your name") String name,
    @DiscordParameter(name = "occupation", description = "Your occupation") String occupation) {
    return "Hello, " + name + "! You are a " + occupation + "!";
}
```

The `@DiscordParameter` works with specific types, and the class provided must be one of the following:

- String
- Integer
- Long
- Double
- Boolean
- User
- Member
- GuildChannelUnion

Commands, Modals, Buttons and Menus can return things in the method. The Software accept:

- void
- String
- MessageCreateData
- Modal
- MessagePollData
- MessageEmbed
- Collection\<MessageEmbed\>
- FileUpload
- Collection\<FileUpload\>
- LayoutComponent
- Collection\<LayoutComponent\>
- TextLocaleResponse

#### Creating an Auto-Complete

You have two options for implementing auto-complete: **choices** and **custom auto-complete**.  
Choices are specified directly in the parameter declaration, while custom auto-complete requires a dedicated function.

##### Choices

Choices are static options predefined in the parameter declaration using the `@DiscordChoice` annotation.

```java
@DiscordCommand(name = "form", description = "Form", ephemeral = true)
public Modal formRegister(@RequestInfo Member member,
                          Guild guild,
                          @DiscordParameter(name = "type", description = "Type of form", choices = {
                                  @DiscordChoice(name = "Validation", value = "validation"),
                                  @DiscordChoice(name = "Report", value = "report")
                          }) String type) {
    if(type.equals("validation")) {
      // Handle validation logic
    } else {
      // Handle report logic
    }
}
```

##### Auto-Complete

Custom auto-complete provides dynamic options based on user input. 
You need to declare the auto-complete logic using the `@DiscordAutoComplete` annotation on a separate method that returns 
filtered options based on user input.

```java
@DiscordCommand(name = "menu", description = "menu command")
public LayoutComponent menu(@DiscordParameter(name = "name", description = "Your name", autoComplete = true) String name,
                            @DiscordParameter(name = "occupation", description = "Your occupation", autoComplete = true) String occupation) {
  // Command logic here
}

@DiscordAutoComplete(name = "menu", variable = "name")
public Command.Choice[] menuAutoCompleteName(AutoCompleteQuery query) {
    List<Command.Choice> choices = List.of(
            new Command.Choice("John Doe", "John Doe"),
            new Command.Choice("Jane Doe", "Jane Doe"),
            new Command.Choice("John Smith", "John Smith")
    );

    return choices.stream().filter(choice -> choice.getName().toLowerCase().contains(query.getValue().toLowerCase())).toArray(Command.Choice[]::new);
}

@DiscordAutoComplete(name = "menu", variable = "occupation")
public Command.Choice[] menuAutoCompleteOccupation(AutoCompleteQuery query) {
    List<Command.Choice> choices = List.of(
            new Command.Choice("Developer", "Developer"),
            new Command.Choice("Designer", "Designer"),
            new Command.Choice("Tester", "Tester")
    );

    return choices.stream().filter(choice -> choice.getName().toLowerCase().contains(query.getValue().toLowerCase())).toArray(Command.Choice[]::new);
}
```

#### Creating a Button

Buttons are interactive components that users can click to trigger specific actions. 
You define buttons using the `@DiscordButton` annotation. 
Each button should have a unique name and can be ephemeral if you want the result to only be visible to the user who clicked it.

Example:

```java
// Somewhere you need to define the button
Button confirm = Button.primary("apply-report-action:" + reportId, "Confirm");

@DiscordButton(name = "apply-report-action", ephemeral = true)
public String applyReportAction(ButtonInteractionEvent event, Guild guild) {
    String id = event.getComponentId().split(":")[1];
    Member reported = guild.getMemberById(id);

    if (reported == null) {
        return "User not found";
    }

    event.getMessage().delete().queue();
    return "Report applied to " + reported.getAsMention();
}
```

In the button creation, you may notice a specific structure used in the button's component ID. For example:

```java
Button confirm = Button.primary("apply-report-action:" + reportId, "Confirm");
```

In this line, the string before the colon (:) represents the button's name or action identifier, 
while the part after the colon is an additional parameter that can provide context or identify the specific instance of the action.

Using this mechanism, you can create multiple buttons for various actions while still passing unique identifiers or 
parameters associated with those actions. 
This approach enhances the flexibility and functionality of button interactions in your Discord bot.

#### Creating a Modal

Modals allow for more complex user input, such as forms. You can define modals using the `@DiscordModal` annotation and display them using the `Modal.create` method.

```java
@DiscordCommand(name = "form", description = "Form", ephemeral = true)
public Modal formRegister(@RequestInfo Member member,
                          Guild guild) {
    return Modal.create("complex-modal-report", guild.getName() + " - Report Form")
            .addActionRow(TextInput.create("report-id", "UserId", TextInputStyle.SHORT).setPlaceholder("12802383984391").build())
            .addActionRow(TextInput.create("report-motivation", "Motivation", TextInputStyle.SHORT).setPlaceholder("He's very silly").build())
            .build();
}
```

In this example:

- A `/form` command is used to register a "Report" form.
- The `Modal` is the response for the command and will be displayed in Discord.

After the user submits the modal, you can handle the input with the @DiscordModal annotation, as shown below:

```java
@DiscordModal(name = "complex-modal-report", ephemeral = true)
public void reportForm(@RequestInfo Member member, Guild guild, JDA jda,
                       ModalInteractionEvent event,
                       @DiscordField("report-id") String reportId,
                       @DiscordField("report-motivation") String reportMotivation) {

    Member reported = guild.getMemberById(reportId);

    if (reported == null) {
        event.reply("User not found").setEphemeral(true).queue();
        return;
    }

    MessageEmbed embed = new EmbedBuilder().setTitle("Report").setColor(Color.RED)
            .setDescription("User: " + reported.getAsMention() + "\nMotivation: " + reportMotivation)
            .setFooter("Reported by: " + member.getEffectiveName(), member.getUser().getAvatarUrl())
            .build();

    Button confirm = Button.primary("apply-report-action:" + reportId, "Confirm");
    Button cancel = Button.danger("cancel-report-action", "Cancel");

    event.replyEmbeds(embed).addActionRow(confirm, cancel).setEphemeral(true).queue();
}
```

In this handler:

- The modal's data is processed, and the member is identified using the reportId.
- If the user is not found, an ephemeral reply is sent to the user.
- A report embed is created with the details and two buttons: "Confirm" and "Cancel."
- The button's component ID includes the reportId for further processing of the button action.

#### Creating a Menu

You can create interactive menus in your Discord bot using the `@DiscordMenu` annotation along with a 
`SelectMenu`. This allows users to select options from a dropdown menu.

Here's how to define a menu command:

```java
@DiscordCommand(name = "menu", description = "menu command")
public LayoutComponent menu() {
  return ActionRow.of(
          StringSelectMenu.create("auto-menu-select")
                  .addOption("foo", "foo")
                  .addOption("bar", "bar")
                  .addOption("baz", "baz")
                  .build()
  );
}
```

In this example:

- The `/menu` command generates a dropdown menu with three options: "foo," "bar," and "baz."
- The `StringSelectMenu` is created with a custom ID, "auto-menu-select," which will be used for handling selections.

```java
@DiscordMenu(name = "auto-menu-select")
    public String selectMenu(StringSelectInteractionEvent event) {
        return "You selected: " + event.getSelectedOptions().get(0).getLabel();
    }
```

#### Internationalization (I18N)

Internationalization allows your bot to support multiple languages and provide localized responses. 
You can implement I18N in your Discord bot using `TextLocaleResponse` or the `LocalizationManager`.

Here’s an example of a translated controller:

```java
@DiscordController(value = "translated", description = "Translated Controller", resource = "translated", locales = DiscordLocale.PORTUGUESE_BRAZILIAN)
public class TranslatedController {

    @DiscordCommand(name = "hello", description = "Hello command", ephemeral = true)
    public TextLocaleResponse hello(@RequestInfo Member member,
                                    @DiscordParameter(name = "name", description = "Your name") String name) {

        return new TextLocaleResponse("translated.hello.response", name);
    }

    @DiscordCommand(name = "bye", description = "Bye command", ephemeral = true)
    public String bye(@RequestInfo Member member,
                      @LocaleType DiscordLocale userLocale,
                      LocalizationManager localization,
                      @DiscordParameter(name = "name", description = "Your name") String name) {

        return localization.getLocalizedString("translated.bye.response", userLocale, name);
    }
}
```

- The `@DiscordController` annotation defines a controller that is set up for localization. 
The resource parameter specifies the resource file used for localization, and locales defines the supported locale 
(in this case, Brazilian Portuguese).
- The hello command uses `TextLocaleResponse` to return a localized message. 
- The message key `translated.hello.response` is used to fetch the appropriate translation for the response based on the locale.
- The bye command utilizes the `LocalizationManager` to retrieve a localized string for the response. 
The userLocale parameter allows the command to adapt to the user's language preference when returning the localized message.

For more information about ResourceBundle, you can refer to [Baeldung's ResourceBundle Guide](https://www.baeldung.com/java-resourcebundle) or [Official JDA Example](https://github.com/discord-jda/JDA/blob/master/src/examples/java/LocalizationExample.java).

### Exceptions

You can handle unexpected exceptions in your Discord bot using the `@DiscordExceptions` annotation. 
This allows you to define a global exception handler for your commands.

##### Generic Exception Handler

Here’s an example of a generic exception handler that logs the error and sends a reply to the user:

```java
@DiscordExceptions
public class GenericExceptionHandler {
    
    @DiscordException(Throwable.class)
    public void onThrowable(Throwable throwable, Interaction interaction) {
        if(interaction instanceof GenericCommandInteractionEvent event)  {
            event.reply("An error occurred: " + throwable.getMessage()).setEphemeral(true).queue();
        }
    }
}
```

In this example:

- The onThrowable method is invoked for any Throwable that is thrown during command execution.

##### Specific Exception Handling

You can also create specific exception handlers for certain classes or packages:

```java
@Component
@DiscordExceptions(classes = {VoiceAgentController.class, VoiceMasterController.class})
public class VoiceExceptionController {

    private final MainExceptionController mainExceptionController;

    public VoiceExceptionController(MainExceptionController mainExceptionController) {
        this.mainExceptionController = mainExceptionController;
    }

    @DiscordException(MissingPermissionsException.class)
    public void missingPermissionsException(Throwable exception, Interaction interaction, LocalizationManager localizationManager, @LocaleType DiscordLocale locale) {
        if(interaction instanceof IReplyCallback callback) {
            callback.reply(localizationManager.getLocalizedString("voice.error.missing_permissions", locale)).setEphemeral(true).queue();
        }
    }

    @DiscordException(CommandNotFoundException.class)
    public void commandNotFoundException(Throwable exception, Interaction interaction, LocalizationManager localizationManager, @LocaleType DiscordLocale locale) {
        if (interaction instanceof IReplyCallback callback) {
            callback.reply(localizationManager.getLocalizedString("voice.error.command_not_found", locale)).setEphemeral(true).queue();
        }
    }

    @DiscordException(Throwable.class)
    public void throwable(Throwable throwable, Interaction interaction, LocalizationManager localization, @LocaleType DiscordLocale locale) {
        if(interaction instanceof IReplyCallback callback) {
            callback.reply(localization.getLocalizedString("voice.error.generic", locale)).setEphemeral(true).queue();
        }

        mainExceptionController.handle(throwable, interaction);
    }
}
```

In this example:

- The `VoiceExceptionController` handles specific exceptions such as `MissingPermissionsException` and `CommandNotFoundException`.
- Each method replies with a localized error message based on the user’s locale.

### Logging Framework - SLF4J

Curupira utilizes [SLF4J](https://www.slf4j.org/) as its logging framework.

To ensure proper logging functionality, it is essential to include an SLF4J implementation in your build path alongside Curupira.
Failure to do so may result in the following messages being displayed on the console during startup:

```
SLF4J: Failed to load class "org.slf4j.impl.StaticLoggerBinder".
SLF4J: Defaulting to no-operation (NOP) logger implementation
SLF4J: See http://www.slf4j.org/codes.html#StaticLoggerBinder for further details.
```

```
SLF4J: No SLF4J providers were found.
SLF4J: Defaulting to no-operation (NOP) logger implementation
SLF4J: See https://www.slf4j.org/codes.html#noProviders for further details.
```
