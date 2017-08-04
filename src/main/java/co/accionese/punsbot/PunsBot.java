package co.accionese.punsbot;

import org.telegram.telegrambots.api.methods.GetFile;
import org.telegram.telegrambots.api.methods.send.SendDocument;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.methods.send.SendPhoto;
import org.telegram.telegrambots.api.objects.*;
import org.telegram.telegrambots.api.objects.replykeyboard.ForceReplyKeyboard;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.exceptions.TelegramApiException;
import org.telegram.telegrambots.logging.BotLogger;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Level;


public class PunsBot extends TelegramLongPollingBot{

    private static final String LOGTAG = "PUNSYBOT";
    private String FLAG = "";
    private ArrayList<String> listOfCommands;
    private ArrayList<String> listOfJokes;
    private GetFile getFileRequest = new GetFile();
    SendMessage finalMessage;
    SendPhoto finalPhoto;
    FileWriter fileWriter;
    boolean isWaitingReply;


    public PunsBot(){
        listOfCommands = new ArrayList<String>();
        listOfJokes = new ArrayList<String>();
        loadCommands();
        loadJokes();
        finalMessage = new SendMessage();
        finalPhoto = new SendPhoto();
        fileWriter = new FileWriter();
        isWaitingReply = false;
    }

    public void onUpdateReceived(Update update) {
        try {
            if(update.hasMessage()){
                Message message = update.getMessage();
                if(message.hasText()){
                    simpleTextCommandHandler(message);
                    if(message.getText().contains(getBotUsername())){
                        finalMessage.setChatId(message.getChatId());
                        finalMessage.setText("Yes how can i /help you?");
                        send(finalMessage);
                    }
                    if(message.getReplyToMessage()!=null){
                        if(message.getReplyToMessage().getFrom().getUserName().equalsIgnoreCase(getBotUsername()) && isWaitingReply==true){
                            finalMessage.setChatId(message.getChatId());
                            if(FLAG.equalsIgnoreCase("REPORT")){
                                finalMessage.setText("I sent your report to my creator, thank you so much for your opinion");
                                send(finalMessage);
                            } else if(FLAG.equalsIgnoreCase("SECRET")){
                                finalMessage.setText("Your Secret is Safe with me");
                                send(finalMessage);
                            } else if (FLAG.equalsIgnoreCase("HAPPY")){
                                finalMessage.setText("Thank You ー( ´ ▽ ` )ﾉ");
                                send(finalMessage);
                            }
                        }
                    }
                }
                else if(message.hasPhoto()){
                    if(message.getCaption().contains(getBotUsername())){
                        finalMessage.setChatId(message.getChatId());
                        finalMessage.setText("Hi "+message.getFrom().getFirstName()+" I'm processing your image, please wait a second and i'll give you an answer");
                        send(finalMessage);
                        boolean isSaved = imagesCommandHandler(message);
                        if(isSaved==false){
                            finalMessage.setText("I'm sorry something went wrong and i couldn't save your image, please use /help to find a solution");
                            send(finalMessage);
                        }
                    }
                }
                else if(message.hasDocument()){
                    //if(message.getCaption().contains(getBotUsername())){
                        finalMessage.setChatId(message.getChatId());
                        finalMessage.setText("Hi "+message.getFrom().getFirstName()+" I'm processing your document, please wait a second and i'll give you an answer");
                        send(finalMessage);
                        boolean isSaved = documentsCommandHandler(message);
                        if(isSaved==false){
                            finalMessage.setText("I'm sorry something went wrong and i couldn't save your image, please use /help to find a solution");
                            send(finalMessage);
                        }
                    //}
                }
            }
        }catch (Exception e) {
            BotLogger.error(LOGTAG, e);
        }
    }

    public String getBotUsername() {
        return "PunsyBot";
    }

    public String getBotToken() {
        return "<token>";
    }

    private void simpleTextCommandHandler(Message message) {
        finalMessage = new SendMessage();
        finalMessage.setChatId(message.getChatId());
        if(message.getText().contains("/help")){
            BotLogger.log(Level.ALL ,LOGTAG, message.getFrom().toString());
            finalMessage = getHelp(finalMessage, message);
            send(finalMessage);
        } else if (message.getText().contains("/joke")){
            BotLogger.log(Level.ALL ,LOGTAG, message.getFrom().toString());
            finalMessage = getJoke(finalMessage);
            send(finalMessage);
        } else if(message.getText().contains("/greet")){
            BotLogger.log(Level.ALL ,LOGTAG, message.getFrom().toString());
            finalMessage = getGreeting(finalMessage);
            send(finalMessage);
        } else if(message.getText().contains("/support")){
            if(message.isGroupMessage()){
                finalMessage.setText("This command is only available in a private conversation");
                send(finalMessage);
            }else{
                BotLogger.log(Level.ALL ,LOGTAG, message.getFrom().toString());
                SendMessage supportMessage = getSupport(finalMessage);
                send(supportMessage);
            }
        } else if(message.getText().contains("/reportproblem")){
            if(message.isGroupMessage()){
                finalMessage.setText("This command is only available in a private conversation");
                send(finalMessage);
            }else{
                BotLogger.log(Level.ALL ,LOGTAG, message.getFrom().toString());
                SendMessage reportMessage = getReportProblem(finalMessage);
                send(reportMessage);
            }
        } else if(message.getText().contains("/cancel")){
            finalMessage = new SendMessage();
            isWaitingReply = false;
        } else if(message.getText().contains("/start")){
            BotLogger.log(Level.ALL ,LOGTAG, message.getFrom().toString());
            finalMessage = getStart(finalMessage, message);
            send(finalMessage);
        }
    }

    private boolean documentsCommandHandler(Message message){
        Document document = message.getDocument();
        SendDocument sendDocument = new SendDocument();
        try {
            getFileRequest.setFileId(document.getFileId());
            File file = getFile(getFileRequest);
            fileWriter.writeDocument(getBotToken(),file.getFilePath());
            sendDocument.setDocument(document.getFileId());
            sendDocument.setChatId(message.getChatId());
            sendDocument.setCaption("Hi again "+message.getFrom().getFirstName()+" your document is now save it");
            sendDocument(sendDocument);
            return true;
        } catch (TelegramApiException e) {
            BotLogger.error(LOGTAG, e);
            return false;
        } catch (MalformedURLException e) {
            BotLogger.error(LOGTAG, e);
            return false;
        }
    }

    private boolean imagesCommandHandler(Message message){
            List<PhotoSize> photos = message.getPhoto();
            // Know file_id
            String f_id = photos.stream()
                    .sorted(Comparator.comparing(PhotoSize::getFileSize).reversed())
                    .findFirst()
                    .orElse(null).getFileId();

            SendPhoto msg = new SendPhoto()
                    .setChatId(message.getChatId())
                    .setPhoto(f_id);
            try {
                getFileRequest.setFileId(f_id);
                File file = getFile(getFileRequest);
                System.out.println(file.getFilePath());
                fileWriter.writeImage(getBotToken(),file.getFilePath());
                // Set photo caption
                String caption = "Hi again " +message.getFrom().getFirstName()+" your image is saved";
                msg.setCaption(caption);
                sendPhoto(msg); // Call method to send the photo with caption
                return true;
            } catch (TelegramApiException e) {
                BotLogger.error(LOGTAG, e);
                return false;
            } catch (MalformedURLException e) {
                BotLogger.error(LOGTAG, e);
                return false;
            }
    }

    private SendMessage getStart(SendMessage startMessage, Message message){
        if(message.isGroupMessage()){
            startMessage.setText("Hello Everyone! my name is PunsyBot, i'm happy to be here."+
                    "\nYou can call me by using @ like @PunsyBot and also by writing \"/\" and see all my commands. You can also use /help"+
                    "\nI hope we can get along ⊂((・▽・))⊃");
        } else {
            startMessage.setText("Hello! my name is PunsyBot, i'm happy to be here."+
                    "\nYou can call me by using @ like @PunsyBot and also by writing \"/\" and see all my commands. You can also use /help"+
                    "\nI hope we can get along \n⊂((・▽・))⊃");
        }
        return startMessage;
    }

    private SendMessage getGreeting(SendMessage greetMessage) {
        greetMessage.setText("Hi");
        return greetMessage;
    }

    private SendMessage getReportProblem(SendMessage specialMessage){
        ForceReplyKeyboard forceReplyKeyboard = new ForceReplyKeyboard();
        specialMessage.setText("Whats the problem with me? \n(´;︵;`), please write the description of the problem if was a mistake please use /cancel");
        specialMessage.setReplyMarkup(forceReplyKeyboard);
        isWaitingReply = true;
        FLAG = "REPORT";
        return specialMessage;
    }

    private void loadCommands(){
        listOfCommands.add("/help");
        listOfCommands.add("/joke");
        listOfCommands.add("/greet");
    }

    private void loadJokes(){
        listOfJokes.add("<b>What do you call the security guards outside of Samsung.</b>\nThe guardians of the galaxy!");
        listOfJokes.add("<b>I think I want a job cleaning mirrors.</b>\nIt's something I could really see myself doing.");
        listOfJokes.add("<b>What do you call a belt made out of watches?</b>\nA waist of time!");
        listOfJokes.add("<b>Two satellites decided to get married.</b>\nThe wedding wasn't much, but the reception was incredible!");
        listOfJokes.add("<b>Did you hear about the guy who invented the knock knock joke?</b>\nHe won the \"no-bell\" prize!");
        listOfJokes.add("<b>What's the difference between a snow man and a snow woman?</b>\nSnow balls!");
        listOfJokes.add("Knock knock.\n" +
                "Who's there?\n" +
                "Ya.\n" +
                "Ya who?\n" +
                "Sorry, I prefer Google.");
        listOfJokes.add("My grandad has the heart of a lion\nand a lifetime ban from the zoo.");
        listOfJokes.add("<b>Why did Cinderella get kicked off the soccer team?</b>\nBecause she kept running from the ball!");

    }

    private SendMessage getHelp(SendMessage helpMessage, Message message){
        if(message.isGroupMessage()){
            helpMessage.setText("I will tell bad Jokes and greet you, this is what a can do: \n\n<b>[Commands]</b>\n\n/joke - i will tell you and awful joke \n/greet - i will say hello and make you day better"+
                    "\n\n<b>[Image Saving]</b> \n\nIf you want me to save an image, just add it to the chat an write in the caption my name @PunsyBot in that way i know that is for me"+
                    "\n\n<b>[Support]</b> \n\nIf you have any problem please call 301-xxx-xxxx or send a email to xxx.xxx@gmail.com \n\nthat's all for now (*≧▽≦)").enableHtml(true);
        }else{
            helpMessage.setText("I will tell bad Jokes and greet you, this is what a can do: \n\n<b>[Commands]</b>\n\n/joke - i will tell you and awful joke \n/greet - i will say hello and make you day better \n/support - I will try to give you more options to help with any problem that you have"+
                    "\n\n<b>[Image Saving]</b> \n\nIf you want me to save an image, just add it to the chat an write in the caption my name @PunsyBot in that way i know that is for me"+
                    "\n\n<b>[Support]</b> \n\nIf you have any problem please call 301-xxx-xxxx or send a email to xxx.xxx@gmail.com \n\nthat's all for now (*≧▽≦)").enableHtml(true);
        }
        return helpMessage;
    }

    private SendMessage getJoke(SendMessage jokeMessage){
        int number = 0 + (int)(Math.random() * (((listOfJokes.size()-1) - 0) + 1));
        jokeMessage.setText(listOfJokes.get(number));
        return jokeMessage;
    }

    private SendMessage getSupport(SendMessage supportMessage){
        supportMessage.setText("Welcome to Support, please choose an option and i will try to help you \n/reportproblem - Report a Problem that i have \n/tellsecret - Tell me a secret \n/tellhappines - Tell me how happy you are that i'm in your life");
        return supportMessage;
    }

    private void send(SendMessage finalMessage){
        try {
            sendMessage(finalMessage.enableHtml(true));
        } catch (TelegramApiException e) {
            BotLogger.error(LOGTAG, e);
        }
    }
}
