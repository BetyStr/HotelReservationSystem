# Hotel Reservation System
Hotel Reservation System for subject PV168, group 03. 
The assignment can be found [here](https://is.muni.cz/auth/el/fi/podzim2020/PV168/projects/Hotel_Reservation_System.pdf) 
in the IS. Design can be found [here](https://xd.adobe.com/view/2187f6e8-afe7-4d06-b23e-f3666b4f32e0-74d4/screen/6a2e17d1-b84f-4499-a0a0-2534d642d5a3). Programmers (in alphabetical order) are: Dominik Dubovský, Tomáš Janoušek,
Andrea Jonásová, Alžbeta Strompová.


## Project Structure
```
src/
  |- main
    |- java.cz.muni.fi.group05.room03
      |- data 
      |- model
      |- ui
        |- form
          | - field
        |- table
          | - util
    |- resources
  | - test
```

### Folders
* **main** - application code splitted in different packages. Main class: Hotel.java.

* **test** - test folder where you can define tests for classes in main folder. 

## Issues
If you encounter an issue you can't solve right away, please add it 
[here](https://gitlab.fi.muni.cz/xdubovs1/hotel-system-reservation/-/issues).
All issues should have form: Name Of Relevant Class - Simple explanation. Use tag *Important*, if you 
think this needs to be fixed as soon as possible.Use tag *Feature*, if it is not necessary to be added 
by assigment, but it is only nice feature to have. After you fix an issue, do not forget to close it. 

## Git workflow
New .gitignore will permit all kinds of files. However, it will not version IntelliJ temporary files. 
We use branches only to mark stable version (master branch) from development branch (develop).

### Download new version
Use git pull to download new versions from faculty GitLab. If there are errors, commit
your local changes and try again.
```
git pull
``` 

### Commit versions
Commit your changes as much as possible. Thanks to our .gitignore you can commit easily.
DO NOT commit your changes to master branch. Commit message should be brief. Referencing
issues is done using a hashtag and issue id.

```
git checkout develop
git add --all
git commit -m "Double reservation creation was disabled (issue #50)"
```

### Push versions
Push new versions at least at the end of your programming session (more often the better). 
If there are errors, commit your local changes, pull and try again to push. DO NOT use
push force, moreover before executing any other command than mentioned, read about what 
it does. Don't lose all of your data as I did.

```
git push
```

### Push to master rejected
Remote will reject all commits to master branch (only merge requests to master are permitted).
To undo last commit you can use following [command](https://www.git-tower.com/learn/git/faq/undo-last-commit/).
Soft flag makes sure the changes in undone revisions are preserved. After running the command, you'll find the 
changes as uncommitted local modifications in your working copy.

```
git reset --soft HEAD~1
```

## Maven
Maven is used for setting up JDK, managing dependencies and running tests. IntelliJ sometimes 
wants to change pom.xml, if it does double check if it is a necessary addition or not. 
Files in .idea are not versioned. Now I will try to discuss some possibilities, why you
might want to change maven file.

### New JDK version
New java versions are generally more stable and provide some additional functions. At first, 
discuss with others on Discord if everyone agrees to move to new version. 
In this example, we changed Java version to 10.
```xml
<properties>
    <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
    <maven.compiler.target>10</maven.compiler.target>
    <maven.compiler.source>10</maven.compiler.source>
</properties>
```

### Adding new dependency
Adding new dependency with Maven is easy, but first discuss with others why you want to add 
this particular extension. If everyone agrees it is a good addition, you can proceed. In 
this example, we added LGoodDatePicker which originated from github, but was added also to Maven.
```xml
<dependency>
    <groupId>com.github.lgooddatepicker</groupId>
    <artifactId>LGoodDatePicker</artifactId>
    <version>11.1.0</version>
</dependency>
```

### Adding new test
All tests are in the test.java folder where you can write your own tests. For basic template,
you can check HotelTest. All tests must end with *Test.java. You don't need to change pom.xml
to write new tests. You can use jUnit for writing basic tests and hamcrest for further matching 
functionality. Both frameworks are added in project and maven. Once you define a new test, it is 
automatically added to the test pool.
