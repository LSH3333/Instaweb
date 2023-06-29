const searchBtn = document.getElementById("search-button");
const serachInput = document.getElementById("search-input")
const inputContainer = document.getElementById("search-input-container");
const searchContainer = document.getElementById("search-container");

searchBtn.addEventListener("click", function () {
    inputContainer.style.display = "block";
    serachInput.focus();
});

serachInput.addEventListener("keyup", function (event) {
    // press enter 
    if (event.keyCode === 13) { 
        var searchQuery = this.value;

        // Send the inputText to the server using AJAX or form submission
        uploadToServer(searchQuery);

        // Reset the input field and hide it
        this.value = "";
        inputContainer.style.display = "none";
    }
});

// Close search-input-container when clicking outside of it
document.addEventListener("click", function (event) {
    let target = event.target;

    if (target != inputContainer && target != searchBtn && target != serachInput && target != searchContainer && target.parentNode != searchBtn) {
        inputContainer.style.display = "none";
    }
});


// 입력한 searchQuery 서버로 전송 
function uploadToServer(searchQuery) {
    const formData = new FormData();

    formData.append("searchQuery", searchQuery);

    const xhr = new XMLHttpRequest();
    xhr.open("POST", "/search/searchAll", true);
    xhr.onload = function () {
        if (xhr.status === 200) {
            console.log("query uploaded successfully");

        } else {
            console.log('Error upload query')
        }
    };

    xhr.onerror = function () {
        console.log('Network error')
    };

    xhr.send(formData);
}