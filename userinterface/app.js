(function ($) {
  "use strict";

  var STORAGE = {
    token: "mrs_token",
    username: "mrs_username",
    role: "mrs_role",
  };

  function authBase() {
    return ($("#api-base-auth").val() || "").trim().replace(/\/+$/, "");
  }

  function riderBase() {
    return ($("#api-base-rider").val() || "").trim().replace(/\/+$/, "");
  }

  function decodeJwtPayload(token) {
    var parts = String(token || "").split(".");
    if (parts.length < 2) {
      return null;
    }
    var b64 = parts[1].replace(/-/g, "+").replace(/_/g, "/");
    while (b64.length % 4) {
      b64 += "=";
    }
    try {
      return JSON.parse(atob(b64));
    } catch (e) {
      return null;
    }
  }

  function isTokenTimeValid(token) {
    var payload = decodeJwtPayload(token);
    if (!payload || typeof payload.exp !== "number") {
      return false;
    }
    return payload.exp * 1000 > Date.now();
  }

  function getSession() {
    return {
      token: sessionStorage.getItem(STORAGE.token),
      username: sessionStorage.getItem(STORAGE.username),
      role: sessionStorage.getItem(STORAGE.role),
    };
  }

  function setSession(token, username, role) {
    sessionStorage.setItem(STORAGE.token, token);
    sessionStorage.setItem(STORAGE.username, username || "");
    sessionStorage.setItem(STORAGE.role, role || "");
  }

  function clearSession() {
    sessionStorage.removeItem(STORAGE.token);
    sessionStorage.removeItem(STORAGE.username);
    sessionStorage.removeItem(STORAGE.role);
  }

  function setAuthStatus(message, kind) {
    var $el = $("#auth-status");
    $el.removeClass("status--error status--ok");
    if (kind) {
      $el.addClass("status--" + kind);
    }
    $el.text(message || "");
  }

  function setRideStatus(message, kind) {
    var $el = $("#ride-status");
    $el.removeClass("status--error status--ok");
    if (kind) {
      $el.addClass("status--" + kind);
    }
    $el.text(message || "");
  }

  function normalizeLonLat(value) {
    return String(value || "")
      .trim()
      .replace(/\s*,\s*/g, ",");
  }

  function refreshSessionUI() {
    var s = getSession();
    var $banner = $("#session-banner");
    var $logout = $("#logout-btn");
    var $requestBtn = $("#request-ride-btn");

    var hasToken = !!(s.token && s.token.length);
    var valid = hasToken && isTokenTimeValid(s.token);
    var isRider = (s.role || "").toUpperCase() === "RIDER";

    if (valid && isRider) {
      $banner
        .removeClass("is-hidden session-banner--warn")
        .addClass("session-banner--rider")
        .text("Rider logged in as " + s.username + ".");
      var $rs = $("#ride-status");
      if (
        $rs.hasClass("status--error") &&
        /rider account|only riders|valid rider session/i.test($rs.text())
      ) {
        setRideStatus("");
      }
    } else if (valid && hasToken) {
      $banner
        .removeClass("is-hidden session-banner--rider")
        .addClass("session-banner--warn")
        .text(
          "Signed in as " +
            s.username +
            " (" +
            (s.role || "unknown role") +
            "). Requesting rides requires a rider account."
        );
    } else if (hasToken && !valid) {
      $banner
        .removeClass("is-hidden session-banner--rider")
        .addClass("session-banner--warn")
        .text("Your session has expired. Please sign in again.");
      clearSession();
      hasToken = false;
      valid = false;
      isRider = false;
    } else {
      $banner.addClass("is-hidden").removeClass("session-banner--rider session-banner--warn").text("");
    }

    if (valid && hasToken) {
      $logout.removeClass("is-hidden");
    } else {
      $logout.addClass("is-hidden");
    }

    var rideOk = valid && isRider;
    $requestBtn.button("option", "disabled", !rideOk);

    if (!rideOk && valid && !isRider) {
      setRideStatus("Use a rider account to request a ride.", "error");
    } else if (!valid) {
      setRideStatus("");
    }
  }

  function loginOnly(username, password) {
    return $.ajax({
      url: authBase() + "/api/auth/login",
      method: "POST",
      contentType: "application/json",
      data: JSON.stringify({ username: username, password: password }),
    });
  }

  function registerThenLogin(username, password, role) {
    return $.ajax({
      url: authBase() + "/api/auth/register",
      method: "POST",
      contentType: "application/json",
      data: JSON.stringify({
        username: username,
        password: password,
        role: role,
      }),
    }).then(function () {
      return loginOnly(username, password);
    });
  }

  function xhrErrorMessage(xhr, baseHint) {
    if (xhr.status === 0) {
      return "Network error (often CORS or wrong base URL). " + baseHint;
    }
    var msg =
      (xhr.responseJSON && xhr.responseJSON.message) ||
      xhr.responseText ||
      xhr.statusText ||
      "Request failed";
    return "Error (" + xhr.status + "): " + msg;
  }

  $(function () {
    $("#register-btn, #signin-btn, #request-ride-btn, #logout-btn").button();
    $("#signin-btn").addClass("ui-button-secondary");

    refreshSessionUI();

    $("#logout-btn").on("click", function () {
      clearSession();
      setAuthStatus("Logged out.", "ok");
      setRideStatus("");
      refreshSessionUI();
    });

    $("#register-form").on("submit", function (e) {
      e.preventDefault();
      var username = ($("#username").val() || "").trim();
      var password = $("#password").val() || "";
      var role = $("#role").val();

      if (!username || !password) {
        setAuthStatus("Please enter username and password.", "error");
        return;
      }

      setAuthStatus("Registering…");
      $("#register-btn, #signin-btn").button("option", "disabled", true);

      registerThenLogin(username, password, role)
        .done(function (loginResponse) {
          var token = loginResponse && loginResponse.token;
          if (token) {
            setSession(token, username, role);
            setAuthStatus("Registered and signed in.", "ok");
            refreshSessionUI();
          } else {
            setAuthStatus("Login did not return a token. Check credentials or server logs.", "error");
          }
        })
        .fail(function (xhr) {
          setAuthStatus(xhrErrorMessage(xhr, "Auth base: " + authBase()), "error");
        })
        .always(function () {
          $("#register-btn, #signin-btn").button("option", "disabled", false);
        });
    });

    $("#signin-btn").on("click", function () {
      var username = ($("#username").val() || "").trim();
      var password = $("#password").val() || "";
      var role = $("#role").val();

      if (!username || !password) {
        setAuthStatus("Please enter username and password.", "error");
        return;
      }

      setAuthStatus("Signing in…");
      $("#register-btn, #signin-btn").button("option", "disabled", true);

      loginOnly(username, password)
        .done(function (loginResponse) {
          var token = loginResponse && loginResponse.token;
          if (token) {
            setSession(token, username, role);
            setAuthStatus("Signed in.", "ok");
            refreshSessionUI();
          } else {
            setAuthStatus("Invalid username or password.", "error");
          }
        })
        .fail(function (xhr) {
          setAuthStatus(xhrErrorMessage(xhr, "Auth base: " + authBase()), "error");
        })
        .always(function () {
          $("#register-btn, #signin-btn").button("option", "disabled", false);
        });
    });

    $("#ride-form").on("submit", function (e) {
      e.preventDefault();
      var s = getSession();
      if (!s.token || !isTokenTimeValid(s.token)) {
        setRideStatus("You need a valid rider session.", "error");
        refreshSessionUI();
        return;
      }
      if ((s.role || "").toUpperCase() !== "RIDER") {
        setRideStatus("Only riders can request a ride.", "error");
        return;
      }

      var pickup = normalizeLonLat($("#pickup-location").val());
      var destination = normalizeLonLat($("#destination-location").val());
      if (!pickup || !destination) {
        setRideStatus("Enter pickup and destination.", "error");
        return;
      }

      setRideStatus("Submitting ride request…");
      $("#request-ride-btn").button("option", "disabled", true);

      $.ajax({
        url: riderBase() + "/api/rider/request",
        method: "POST",
        contentType: "application/json",
        headers: { Authorization: "Bearer " + s.token },
        data: JSON.stringify({
          pickupLocation: pickup,
          destination: destination,
        }),
      })
        .done(function (tripId) {
          setRideStatus("Ride requested. Trip id: " + tripId + ".", "ok");
        })
        .fail(function (xhr) {
          setRideStatus(xhrErrorMessage(xhr, "Rider base: " + riderBase()), "error");
        })
        .always(function () {
          refreshSessionUI();
        });
    });
  });
})(jQuery);
