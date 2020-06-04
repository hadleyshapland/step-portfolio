/*!
    * Start Bootstrap - Resume v6.0.0 (https://startbootstrap.com/template-overviews/resume)
    * Copyright 2013-2020 Start Bootstrap
    * Licensed under MIT (https://github.com/BlackrockDigital/startbootstrap-resume/blob/master/LICENSE)
    */
    (function ($) {
    "use strict"; // Start of use strict

    // Smooth scrolling using jQuery easing
    $('a.js-scroll-trigger[href*="#"]:not([href="#"])').click(function () {
        if (
            location.pathname.replace(/^\//, "") ==
                this.pathname.replace(/^\//, "") &&
            location.hostname == this.hostname
        ) {
            let target = $(this.hash);
            target = target.length
                ? target
                : $("[name=" + this.hash.slice(1) + "]");
            if (target.length) {
                $("html, body").animate(
                    {
                        scrollTop: target.offset().top,
                    },
                    1000,
                    "easeInOutExpo"
                );
                return false;
            }
        }
    });

    // Closes responsive menu when a scroll trigger link is clicked
    $(".js-scroll-trigger").click(function () {
        $(".navbar-collapse").collapse("hide");
    });

    // Activate scrollspy to add active class to navbar items on scroll
    $("body").scrollspy({
        target: "#sideNav",
    });
})(jQuery); // End of use strict


function loadComments() {
  let num = document.getElementById("number").value;
  if (num == null) {
    num = "5";
  }

  fetch('/comments?number-comments=' + num)
  .then(response => response.json())
  .then((comments) => 
  {
    let commentListElement = document.getElementById('comment-list');
    commentListElement.innerHTML = "";
        
    comments.forEach((comment) => 
    {
      commentListElement.appendChild(createCommentElement(comment));
    })
  });
}


function createCommentElement(comment) {
  const commentElement = document.createElement('li');
  commentElement.className = 'comment';

  const textElement = document.createElement('span');
  const date = new Date(comment.timestamp);
  textElement.innerText = comment.text + " - " + comment.name + " on " + date;

  const deleteButtonElement = document.createElement('button');
  deleteButtonElement.innerText = 'X';
  deleteButtonElement.addEventListener('click', () => {
    deleteComment(comment.id);
    commentElement.remove();
  });

  commentElement.appendChild(textElement);
  commentElement.appendChild(deleteButtonElement);

  return commentElement;
}

function deleteComment(id) {
  const params = new URLSearchParams();
  params.append('id', id);
  fetch('/delete-comment', {method: 'POST', body: params});
}