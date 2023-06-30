const searchMineBtn = document.getElementById("search-mine-button");
const searchMineTextBtn = document.getElementById("search-mine-text-button");
const serachMineInput = document.getElementById("search-mine-input")
const inputMineContainer = document.getElementById("search-mine-input-container");
const searchMineContainer = document.getElementById("search-mine-container");

searchMineBtn.addEventListener("click", function () {
    inputMineContainer.style.display = "block";
    serachMineInput.focus();
});

searchMineTextBtn.addEventListener("click", function () {
    inputMineContainer.style.display = "block";
    serachMineInput.focus();
});

serachMineInput.addEventListener("keyup", function (event) {
    // press enter 
    if (event.keyCode === 13) {
        let searchQuery = this.value;
        window.location.href = '/search/resultMineList?searchQuery=' + encodeURIComponent(searchQuery);

        // Reset the input field and hide it
        this.value = "";
        inputMineContainer.style.display = "none";
    }
});

// Close search-input-container when clicking outside of it
document.addEventListener("click", function (event) {
    let target = event.target;

    if (target != inputMineContainer && target != searchMineBtn && target != searchMineTextBtn && target != serachMineInput 
        && target != searchMineContainer && target.parentNode != searchMineBtn) {
        inputMineContainer.style.display = "none";
    }
});
