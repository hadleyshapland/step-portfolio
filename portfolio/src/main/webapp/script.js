// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

function addRandomGreeting() {
  const greetings = [
    'Hello world!', 
    '¡Hola Mundo!', 
    '你好，世界！', 
    'Bonjour le monde!'
  ];

  const greeting = greetings[Math.floor(Math.random() * greetings.length)];

  const greetingContainer = document.getElementById('greeting-container');
  greetingContainer.innerText = greeting;
}

function addRandomFact() {
  const facts = [
    'I have 2 dogs, Olive and Bella', 
    'I am turning 20 on June 30th', 
    'I live on a farm',
    'I switched my major from Biomedical Engineering to CS at the end of my first year',
    'I have never been to the West Coast', 
    'My dogs have an instagram: @olive_and_bella'
  ];

  const fact = facts[Math.floor(Math.random() * facts.length)];

  const factContainer = document.getElementById('fact-container');
  factContainer.innerText = fact;
}