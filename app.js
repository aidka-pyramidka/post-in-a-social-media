function esc(s) {
  return String(s)
    .replaceAll("&", "&amp;")
    .replaceAll("<", "&lt;")
    .replaceAll(">", "&gt;")
    .replaceAll('"', "&quot;")
    .replaceAll("'", "&#39;");
}

function fmtDate(iso) {
  if (!iso) return "";
  const d = new Date(iso);
  if (Number.isNaN(d.getTime())) return String(iso);
  return d.toLocaleString();
}

function setStatus(msg) {
  $("#statusText").text(msg || "");
}

function showError($el, msg) {
  if (!msg) {
    $el.attr("hidden", true).text("");
    return;
  }
  $el.removeAttr("hidden").text(msg);
}

async function loadItems() {
  setStatus("Loading...");
  try {
    const items = await $.getJSON("/items");
    renderItems(items);
    setStatus(`Loaded ${items.length} item(s)`);
  } catch (e) {
    setStatus("Failed to load");
  }
}

function renderItems(items) {
  const rows = items.map((it) => {
    const idShort = String(it.id).slice(0, 8) + "…";
    return `
      <tr>
        <td title="${esc(it.id)}"><code>${esc(idShort)}</code></td>
        <td>${esc(it.author)}</td>
        <td><code>${esc(it.visibility)}</code></td>
        <td>${esc(it.likes)}</td>
        <td class="tiny">${esc(fmtDate(it.createdAt))}</td>
        <td class="tiny">${esc(fmtDate(it.updatedAt))}</td>
        <td>${esc(it.content)}</td>
        <td>
          <button class="secondary" data-action="edit" data-id="${esc(it.id)}">Edit</button>
          <button class="danger" data-action="delete" data-id="${esc(it.id)}">Delete</button>
        </td>
      </tr>
    `;
  });

  $("#itemsBody").html(rows.join(""));
}

function openModal() {
  $("#modalBackdrop").removeAttr("hidden");
  $("#editModal").removeAttr("hidden");
}

function closeModal() {
  $("#modalBackdrop").attr("hidden", true);
  $("#editModal").attr("hidden", true);
  showError($("#editError"), null);
}

async function fetchItem(id) {
  return await $.getJSON(`/items/${encodeURIComponent(id)}`);
}

async function createItem(payload) {
  return await $.ajax({
    method: "POST",
    url: "/items",
    contentType: "application/json",
    data: JSON.stringify(payload),
  });
}

async function updateItem(id, payload) {
  return await $.ajax({
    method: "PUT",
    url: `/items/${encodeURIComponent(id)}`,
    contentType: "application/json",
    data: JSON.stringify(payload),
  });
}

async function deleteItem(id) {
  return await $.ajax({
    method: "DELETE",
    url: `/items/${encodeURIComponent(id)}`,
  });
}

$(async function () {
  await loadItems();

  $("#refreshBtn").on("click", loadItems);

  $("#createForm").on("submit", async function (e) {
    e.preventDefault();
    showError($("#createError"), null);

    const payload = {
      author: $(this).find("[name=author]").val(),
      content: $(this).find("[name=content]").val(),
      visibility: $(this).find("[name=visibility]").val(),
      likes: Number($(this).find("[name=likes]").val() || 0),
    };

    try {
      await createItem(payload);
      this.reset();
      $(this).find("[name=likes]").val("0");
      await loadItems();
    } catch (err) {
      const msg = err?.responseJSON?.error || "Failed to create";
      showError($("#createError"), msg);
    }
  });

  $("#itemsBody").on("click", "button[data-action]", async function () {
    const action = $(this).data("action");
    const id = $(this).data("id");

    if (action === "delete") {
      if (!confirm("Delete this post?")) return;
      try {
        await deleteItem(id);
        await loadItems();
      } catch (err) {
        alert(err?.responseJSON?.error || "Delete failed");
      }
      return;
    }

    if (action === "edit") {
      try {
        const it = await fetchItem(id);
        const $f = $("#editForm");
        $f.find("[name=id]").val(it.id);
        $f.find("[name=author]").val(it.author);
        $f.find("[name=content]").val(it.content);
        $f.find("[name=visibility]").val(it.visibility);
        $f.find("[name=likes]").val(String(it.likes));
        openModal();
      } catch (err) {
        alert(err?.responseJSON?.error || "Failed to load item");
      }
    }
  });

  $("#closeModalBtn, #cancelEditBtn, #modalBackdrop").on("click", closeModal);

  $("#editForm").on("submit", async function (e) {
    e.preventDefault();
    showError($("#editError"), null);
    const id = $(this).find("[name=id]").val();
    const payload = {
      author: $(this).find("[name=author]").val(),
      content: $(this).find("[name=content]").val(),
      visibility: $(this).find("[name=visibility]").val(),
      likes: Number($(this).find("[name=likes]").val() || 0),
    };

    try {
      await updateItem(id, payload);
      closeModal();
      await loadItems();
    } catch (err) {
      const msg = err?.responseJSON?.error || "Failed to update";
      showError($("#editError"), msg);
    }
  });
});


