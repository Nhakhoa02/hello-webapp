function fetchObjectWithRole(key) {
    const url = "http://54.83.79.139:8080/hello-webapp/object-with-role/" + key;
    fetch(url)
    .then (response => response.blob())
    .then (blob => {
        const objectURL = URL.createObjectURL(blob);
        const image_s3 = document.getElementById("image_s3");
        image_s3.src = objectURL;
    });
}

// Load list of objects from server. If the server endpoint is not available,
// fall back to a small sample list so the UI still renders.
function loadImageList() {
    const listUrl = "http://54.83.79.139:8080/hello-webapp/object-list";
    fetch(listUrl)
    .then(res => {
        if (!res.ok) throw new Error('Network response was not ok');
        return res.json();
    })
    .then(json => {
        // expect json to be an array of object keys (strings)
        renderTable(json);
    })
    .catch(err => {
        console.warn('Could not load list from server, using sample data:', err.message);
        const sample = ["qr.png", "sample1.jpg", "sample2.png"];
        renderTable(sample);
    });
}

function renderTable(keys) {
    const tbody = document.getElementById('images_tbody');
    tbody.innerHTML = '';
    keys.forEach(key => {
        const tr = document.createElement('tr');
        const nameTd = document.createElement('td');
        nameTd.textContent = key;

        const actionTd = document.createElement('td');
        const viewBtn = document.createElement('button');
        viewBtn.textContent = 'View';
        viewBtn.addEventListener('click', () => {
            // show preview using existing endpoint
            const preview = document.getElementById('preview');
            fetch('http://54.83.79.139:8080/hello-webapp/object-with-role/' + key)
                .then(r => r.blob())
                .then(b => preview.src = URL.createObjectURL(b))
                .catch(e => alert('Failed to fetch image: ' + e.message));
        });

        const downloadBtn = document.createElement('button');
        downloadBtn.textContent = 'Download';
        downloadBtn.addEventListener('click', () => downloadImage(key));

        const deleteBtn = document.createElement('button');
        deleteBtn.textContent = 'Delete';
        deleteBtn.style.marginLeft = '6px';
        deleteBtn.addEventListener('click', () => {
            if (!confirm('Delete "' + key + '"?')) return;
            deleteObject(key);
        });

    actionTd.appendChild(viewBtn);
    actionTd.appendChild(document.createTextNode(' '));
    actionTd.appendChild(downloadBtn);
    actionTd.appendChild(deleteBtn);

        tr.appendChild(nameTd);
        tr.appendChild(actionTd);
        tbody.appendChild(tr);
    });
}

function downloadImage(key) {
    const url = 'http://54.83.79.139:8080/hello-webapp/object-download/' + key;
    fetch(url)
    .then(res => {
        if (!res.ok) throw new Error('Network response was not ok');
        return res.blob();
    })
    .then(blob => {
        const a = document.createElement('a');
        const objectURL = URL.createObjectURL(blob);
        a.href = objectURL;
        a.download = key;
        document.body.appendChild(a);
        a.click();
        a.remove();
        // revoke after a short delay
        setTimeout(() => URL.revokeObjectURL(objectURL), 1000);
    })
    .catch(err => alert('Download failed: ' + err.message));
}

// Very basic upload handler. This attempts to POST the file to a server endpoint
// at /hello-webapp/upload. If that endpoint doesn't exist, we show an alert.
function handleUpload() {
    const input = document.getElementById('file_input');
    if (!input.files || input.files.length === 0) {
        alert('Please select a file to upload');
        return;
    }
    const files = Array.from(input.files);
    const uploadUrl = 'http://54.83.79.139:8080/hello-webapp/object-upload';
    const form = new FormData();
    // append each file with the same field name 'file' so the servlet receives multiple parts
    files.forEach(f => form.append('file', f, f.name));

    fetch(uploadUrl, { method: 'POST', body: form })
    .then(res => {
        if (!res.ok) throw new Error('Upload failed: ' + res.statusText);
        // servlet returns plain text summarizing uploaded files
        return res.text();
    })
    .then(text => {
        alert('Upload response:\n' + text);
        loadImageList();
    })
    .catch(err => {
        // If server not available, just inform user and keep sample data
        alert('Upload failed or not supported by server: ' + err.message);
    });
}

// Delete a single object (per-row) using HTTP DELETE on the server.
function deleteObject(key) {
    const url = 'http://54.83.79.139:8080/hello-webapp/object-delete/' + key;
    fetch(url, { method: 'DELETE' })
    .then(res => {
        if (!res.ok) throw new Error('Delete failed: ' + res.statusText);
        return res.text();
    })
    .then(text => {
        alert('Delete response:\n' + text);
        loadImageList();
    })
    .catch(err => alert('Delete failed: ' + err.message));
}