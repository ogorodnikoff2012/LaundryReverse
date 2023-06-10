let faviconUpdateIntervalId = null;
const FAVICON_N_FRAMES = 25;

function zeroPad(num, size) {
  return String(num).padStart(size, '0');
}

$(document).ready(async () => {
  await createWidgets();
  let $faviconCurrentFrame = 8;
  faviconUpdateIntervalId = setInterval(() => {
    $("#favicon").attr("href", "/media/laundry" + zeroPad($faviconCurrentFrame, 2) + ".png");
    $faviconCurrentFrame += 8;
    $faviconCurrentFrame %= FAVICON_N_FRAMES;
  }, 1000);
});

async function createWidgets() {
  appendWidget(await createLayoutInspectorWidget());
  appendWidget(await createApiWidget());
}

function appendWidget(widget) {
  $("#main").append(widget);
}

async function createApiWidget() {
  const driverList = await getDriverList();
  const list = $("<ul>");
  for (const driverName of driverList) {
    const driver = $("<li>").append($("<p>").text(driverName));
    driver.append(await createDriverWidget(driverName));
    list.append(driver);
  }

  return $("<div>")
  .append($("<h2>").text("Driver methods"))
  .append(list)
  .append($("<h2>").text("Method call result ").append(
      $("<input type='button' value='Clear'>").click(clearMethodCallResult)))
  .append($("<div id='method-call-result'>").append(
      "<img style='height: 25vh' hidden>").append("<div hidden>"));
}

function createLayoutInspectorWidget() {
  return $("<div>")
  .append($("<h2>").text("Layout inspector"))
  .append(createLayoutInspectorViewWidget())
  .append(createLayoutInspectorTreeWidget())
  .append(
      $("<input type='button' value='Refresh'>").click(refreshLayoutInspector));
}

function createLayoutInspectorViewWidget() {
  return $("<div id='layout-inspector-view-container'>").append(
      $("<svg style='border: 1px solid black' width='25vw' viewBox='0 0 1080 1920' id='layout-inspector-view' xmlns=\"http://www.w3.org/2000/svg\" version=\"1.1\">")
      .append($("<image>"))
      .append($("<polygon points='0,0' fill='rgba(255, 0, 0, 0.3)' stroke='red'>")));
}

function createLayoutInspectorTreeWidget() {
  return $("<div>").append($("<form id='layout-inspector-tree'>"));
}

function refreshLayoutInspectorView() {
  const html = $("#layout-inspector-view-container").html();
  $("#layout-inspector-view-container").html(html);
}

async function refreshLayoutInspector() {
  const [screenshot, tree] = await Promise.all([
    getScreenshot(), getUiInfo()
  ]);

  const EMPTY_RECT = { x: 0, y: 0, width: 0, height: 0 };

  const selectNode = ({x, y, width, height}) => {
    $("#layout-inspector-view polygon").attr("points",
        [[x, y], [x + width, y], [x + width, y + height], [x, y + height]]
        .map(value => value.join(","))
        .join(" ")
    );
    refreshLayoutInspectorView();
  };

  const buildTree = (node) => {
    let title = node.clazz;
    if (node.resourceId) {
      title += ` (id=${node.resourceId})`;
    }
    if (node.text) {
      title += ": " + node.text;
    }
    if (node.contentDesc) {
      title += ` (${node.contentDesc})`;
    }

    const radioButton = $("<input type='radio' name='tree_node'>").click(() => selectNode(node.bounds));
    const treeNode = $("<li>").append(
        $("<div>").append(title).append(" ").append(radioButton).append(" " + node.rawBounds));
    if (node.children.length > 0) {
      const children = $("<ul>");
      for (const child of node.children) {
        children.append(buildTree(child));
      }
      treeNode.append(children);
    }

    return treeNode;
  };

  $("#layout-inspector-view image").attr("href", screenshot.imgBase64);
  $("#layout-inspector-tree").empty().append(
      $("<ul>").append(buildTree(tree.node)));
  setTimeout(() => selectNode(EMPTY_RECT));
}

function clearMethodCallResult() {
  $("#method-call-result").children().hide();
}

function showMethodCallResult(resultType, result) {
  if (resultType === "img") {
    $("#method-call-result").children().hide();
    $("#method-call-result > img").attr("src", result.imgBase64).show();
  } else {
    $("#method-call-result").children().hide();
    $("#method-call-result > div").text(JSON.stringify(result)).show();
  }
}

async function createDriverWidget(driverName) {
  const driverWidget = $("<ul>");
  const driverApi = await getDriverApi(driverName);

  for (const method of driverApi.methods) {
    driverWidget.append(
        $("<li>").append(createMethodWidget(driverApi.driverName, method)));
  }
  return driverWidget;
}

function computeResultType(result) {
  return (typeof result === "object" && result !== null && "imgBase64"
      in result) ? "img" : "text";
}

function callApiMethod(driver, method, methodArguments) {
  $.ajax("/console/driver/call",
      {
        data: JSON.stringify({driver, method, arguments: methodArguments}),
        contentType: "application/json",
        type: "POST",
      }).done((result) => {
    showMethodCallResult(computeResultType(result), result);
  }).fail((err) => {
    showMethodCallResult("text", err.responseJSON);
  });
}

function createMethodWidget(driverName, method) {
  const widget = $("<div>");
  widget
  .append($("<span>").text(method.returnType))
  .append(" ")
  .append($("<span>").text(method.name))
  .append("(");

  const parameterFields = [];
  for (let i = 0; i < method.parameterTypes.length; ++i) {
    if (i > 0) {
      widget.append(", ");
    }

    const field = $("<input type='text'>");
    widget
    .append($("<span>").text(method.parameterTypes[i]))
    .append(" ")
    .append(field);
    parameterFields.push(field);
  }
  widget.append(") ");

  const button = $("<input type='button'>").val("Run");
  button.click(() => {
    const arguments = parameterFields.map(field => field.val());
    callApiMethod(driverName, method, arguments);
  });
  widget.append(button);

  return widget;
}

function getDriverList() {
  return new Promise((resolve, reject) => {
    $.get("/console/driver/list", (data) => {
      resolve(data);
    }).fail(reject);
  });
}

function getScreenshot() {
  return new Promise((resolve, reject) => {
    $.get(`/console/driver/api/ui/screenshot`, (data) => {
      resolve(data);
    }).fail(reject);
  });
}

function getUiInfo() {
  return new Promise((resolve, reject) => {
    $.get(`/console/driver/api/ui/ui_info`, (data) => {
      resolve(data);
    }).fail(reject);
  });
}

function getDriverApi(driverName) {
  return new Promise((resolve, reject) => {
    $.get(`/console/driver/api/${driverName}`, (data) => {
      resolve(data);
    }).fail(reject);
  });
}