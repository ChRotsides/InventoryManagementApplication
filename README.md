# Inventory Management Application

Android Database Handler and Fragment Interaction Sample App with Inventory Management
### [App Demo Video](https://youtu.be/7g0d2y5ik3w)


## Introduction
This is an Android application created to showcase the interaction between SQLite database operations and UI components via Fragments. The application also demonstrates inventory management through a user-friendly interface.

## Getting Started
These instructions will provide a copy of the project to run on your local machine for development and testing purposes.

## Prerequisites
Android Studio 4.0+ or any later version.
Android SDK.
A device/emulator running Android API level 21 or higher for testing.
Installation
Clone this repository and import it into Android Studio.

Copy code
```git clone https://github.com/ChRotsides/InventoryManagementApp.git```
## Code Overview
### DBHandler.kt
This is the database handler class, DBHandler, which extends SQLiteOpenHelper. This class contains methods for handling the database operations such as creating, reading, updating, and deleting (CRUD operations) records in a SQLite database. The operations are performed on an Item table, storing and manipulating data such as item name, quantity, and price.

### FirstFragment.kt
This is a Fragment class, FirstFragment, which sets its layout view with a binding object. It displays a Snackbar message when a button is clicked. This Fragment also employs a ViewModel to update a count variable and reflect the change in a TextView through LiveData and data binding. The interactions with the SQLite database, using the DBHandler class, occur within this fragment.

This Fragment presents the user with a form for adding new items to the inventory. It includes fields for item name, quantity, and price. Upon submission, these values are stored in the SQLite database. The fragment also contains features for calculating the total value of items in the inventory, searching for items, and a scrollable view to display the list of items.

### Layout XML
The layout for FirstFragment is defined in an XML file. It uses several views and view groups, including LinearLayout, TextInputLayout, TextView, MaterialButton, and ScrollView. It uses the Material Components theme to give the app a modern, aesthetically pleasing look.

There are separate input fields for the item name, quantity, and price, and a button for adding the item to the inventory. A TextView is used to display the total value of the inventory. There's also a search bar for searching items in the inventory, and a scrollable list to display the items in the inventory.

All input fields use the TextInputLayout view to provide a material design styled outline and hint animation.

The item list is a dynamically populated LinearLayout inside a ScrollView, where each item entry in the list is added programmatically in the runtime based on the data retrieved from the SQLite database.

## Built With
1. Kotlin
2. Android Jetpack - A suite of libraries, tools, and guidance to assist developers in writing high-quality apps easier.
3. SQLite - SQLite is a lightweight database that is included with Android and is ideal for local storage.
4. Data Binding - The Data Binding Library allows for binding of UI components in layouts to data sources within the app using a declarative format rather than programmatically.

