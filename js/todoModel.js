/*jshint quotmark:false */
/*jshint white:false */
/*jshint trailing:false */
/*jshint newcap:false */
var app = app || {};

(function () {
	'use strict';

	var Utils = app.Utils;
	// Generic "model" object. You can use whatever
	// framework you want. For this application it
	// may not even be worth separating this logic
	// out, but we do this to demonstrate one way to
	// separate out parts of your application.
	app.TodoModel = function (key) {
		this.key = key;
		this.todos = [];
		this.updateTodoList();
		this.onChanges = [];
	};

	app.TodoModel.prototype.updateTodoList = function() {
		$.getJSON('https://spark-server-with-mongo.herokuapp.com/todos')
		.done(function(response, code) {
			this.todos = response || [];
			this.inform();
			$('.todoapp').loadingOverlay('remove');
		}.bind(this));
	};

	app.TodoModel.prototype.subscribe = function (onChange) {
		this.onChanges.push(onChange);
	};

	app.TodoModel.prototype.inform = function () {
		Utils.store(this.key, this.todos);
		this.onChanges.forEach(function (cb) { cb(); });
	};

	app.TodoModel.prototype.addTodo = function (title) {
		$('.todoapp').loadingOverlay();
		$.ajax({
			method: 'POST',
			contentType: 'application/json',
			url: 'https://spark-server-with-mongo.herokuapp.com/todos',
			data: JSON.stringify({
				title: title
			})
		}).done(function(response, code) {
			this.updateTodoList()
		}.bind(this));
	};

	app.TodoModel.prototype.toggleAll = function (checked) {
		this.graphql(`
			mutation {
				toggleAll (checked: ${checked}) {
					id,
					title,
					completed
				}
			}
		`);
	};

	app.TodoModel.prototype.toggle = function (todoToToggle) {
		$('.todoapp').loadingOverlay();
		$.ajax({
			method: 'PUT',
			contentType: 'application/json',
			url: `https://spark-server-with-mongo.herokuapp.com/todos/${todoToToggle._id.$oid}`,
			data: JSON.stringify({
				completed: !todoToToggle.completed
			})
		}).done(function(response, code) {
			this.updateTodoList()
		}.bind(this));
	};

	app.TodoModel.prototype.destroy = function (todo) {
		$('.todoapp').loadingOverlay();
		$.ajax({
			method: 'DELETE',
			contentType: 'application/json',
			url: `https://spark-server-with-mongo.herokuapp.com/todos/${todo._id.$oid}`
		}).done(function(response, code) {
			this.updateTodoList()
		}.bind(this));
	};

	app.TodoModel.prototype.save = function (todoToSave, text) {
		$('.todoapp').loadingOverlay();
		$.ajax({
			method: 'PUT',
			contentType: 'application/json',
			url: `https://spark-server-with-mongo.herokuapp.com/todos/${todoToSave._id.$oid}`,
			data: JSON.stringify({
				title: text
			})
		}).done(function(response, code) {
			this.updateTodoList()
		}.bind(this));
	};

	app.TodoModel.prototype.clearCompleted = function () {
		this.graphql(`
			mutation {
				clearCompleted {
					id,
					title,
					completed
				}
			}
		`);
	};

})();
