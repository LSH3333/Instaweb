const searchBtn = document.getElementById("search-button");
const searchTextBtn = document.getElementById("search-text-button");
const serachInput = document.getElementById("search-input")
const inputContainer = document.getElementById("search-input-container");
const searchContainer = document.getElementById("search-container");

searchBtn.addEventListener("click", function () {
    inputContainer.style.display = "block";
    serachInput.focus();
});

searchTextBtn.addEventListener("click", function () {
    inputContainer.style.display = "block";
    serachInput.focus();
});

serachInput.addEventListener("keyup", function (event) {
    // press enter 
    if (event.keyCode === 13) {
        let searchQuery = this.value;
        // Check if searchQuery contains backslash
        if (searchQuery.includes('\\')) {
            // Remove backslash from searchQuery
            searchQuery = searchQuery.replace('\\', '');
        }
        window.location.href = '/search/resultList?searchQuery=' + encodeURIComponent(searchQuery);

        // Reset the input field and hide it
        this.value = "";
        inputContainer.style.display = "none";
    }
});

// Close search-input-container when clicking outside of it
document.addEventListener("click", function (event) {
    let target = event.target;

    if (target != inputContainer && target != searchBtn && target != searchTextBtn && target != serachInput
        && target != searchContainer && target.parentNode != searchBtn) {
        inputContainer.style.display = "none";
    }
});
