# ArkhamExBot

This bot is a comprehensive tool for interacting with the **Arkham Exchange**, written in Kotlin. It supports both **console** and **Telegram bot** interfaces, providing an easy way to manage accounts and automate tasks. The bot contains three main features:

1. **View Account Balance**: Check your account's USDT balance and other relevant details.
2. **Generate Spot & Perp Trading Volume**: Automate trading to generate a desired trading volume.
3. **View Account Statistics**: Fetch detailed statistics for your account activity.

---

## Features

### 1. View Account Balance
Retrieve your account balance details, including USDT and other supported tokens.

### 2. Generate Trading Volume
Automatically execute trades on spot & perp markets(It depends on leverage) to increase trading volume, useful for meeting exchange requirements.

### 3. View Account Statistics
Fetch and display account-level statistics such as total trading volume and fees incurred.

---

## How to Use

### Console Version

1. **Configure Accounts**  
   Add your account details in a file named `accounts.txt`. Each account should be added in the following format:
   _ACCOUNT_ID(Any string):API_KEY:API_SECRET_KEY:PROXY_IP:PROXY_PORT:PROXY_USERNAME:PROXY_PASSWORD_

2. **Run the Application**  
Execute the application in your console. Use the interactive menu to select the desired functionality:
- View balances
- Start trading
- View statistics

---

### Telegram Bot Version

It may be online [ArkhamExBot](https://t.me/ArkhamExBot). Or contact the [developer](https://t.me/deni_rodionov)

1. **Set Up a Telegram Bot**  
- Create a new bot using Telegram's [BotFather](https://core.telegram.org/bots#botfather).
- Follow this [guide](https://core.telegram.org/bots/tutorial) for instructions on bot creation.
- Copy the `ACCESS_TOKEN` provided by BotFather.

2. **Configure the Bot**  
Create a `TelegramBotConfig` file and add the bot token in the following format:  
```
object TelegramBotConfig {
    const val ACCESS_TOKEN = "YOUR_BOT_TOKEN"
}
```

3. **Run the Application** 
Start the bot, and interact with it using the following commands:
 - /start - Initialize the session.
 - Use the inline menu to select:
   - View account balances.
   - Start trading volume generation.
   - View account statistics.

## Donations
If you find this project helpful and would like to support its development, consider making a donation:
[![Buy Me a Coffee](https://cdn.buymeacoffee.com/buttons/v2/default-yellow.png)](https://www.buymeacoffee.com/antiglobalist)

Arbitrum USDT 0x3E92ac8A955c0CcaA3abE350A7097b4e8aAFB5c5

Your support is greatly appreciated!

**Developed by** [Antiglobalist](https://t.me/deni_rodionov)
