## Library Server

The second part of my coursework project. It is a web application for administrating library and it's based on MVC pattern.
In a few words, it's kind of a simple CRUD application but it also implements more complex functionality - it automatizes a task
of making up a recommendation list for a reader based on his/her preferences and then it sends this list to an email. 

#### What it's used for?

The module depends on [core module](https://github.com/kojote-git/coursework) and provides an API for manipulating the entities
defined in that module. The API is based on HTTP protocol and has a specific structure. 

Simply speaking, with this API, for example, you can upload books to and download books from the server, add works and authors
to database, search for specific information etc.

#### API

As said above, the module provides the user (administrator) with an API. Despite the fact that it shares some similarities with 
REST API, it's better not to consider the application as RESTFUL. The structure is following:

| URI pattern | HTTP METHOD | DESCRIPTION |
|-|-|-|
| `/rest/{entity}` | **GET** | return all entities |
| `/rest/{entity}/{id}` | **GET** | return a specific entity identified with id |
| `/rest/{entity}/{id}/editing` | **PUT** | edit the entity |
| `/rest/{entity}/{id}/deleting` | **DELETE** | delete the entity |
| `/rest/{entity}/creation` | **POST** | create new entity |

The enties are:
- authors
- works
- books
- instances - instance of a book
- publishers
- subjects


Also, some entities may aggregate other entities. For example, author can have several works. In this scenario, works can be
retrieved using the uri `/rest/authors/{id}/works`. The same is true for any other entities.
