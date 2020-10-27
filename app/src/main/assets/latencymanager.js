(function() {
    // body of the function
    const HAVE_NOTHING = 0;
    const HAVE_METADATA = 1;
    const HAVE_CURRENT_DATA = 2;
    const HAVE_FUTURE_DATA = 3;
    const HAVE_ENOUGH_DATA = 4;

    THEOplayer.TimeServer = function(timeserverUri, syncinterval) {
        this.latency = 0;
        this.rtt = 0;
        this.syncedTimestamp;
        this.interval = 0;
        if (syncinterval != undefined)
            this.interval = syncinterval * 1000;

        this.synctimeout = undefined;
        this.listeners = {};
        this.timeserver = "https://time.theoplayer.com";
        if (timeserverUri !== undefined && timeserverUri !== "")
            this.timeserver = timeserverUri;


        this.listeners = [];
        this.addEventListener = function(type, callback) {
            if (!(type in this.listeners)) {
                this.listeners[type] = [];
            }
            this.listeners[type].push(callback);
        };

        this.removeEventListener = function(type, callback) {
            if (!(type in this.listeners)) {
                return;
            }
            var stack = this.listeners[type];
            for (var i = 0, l = stack.length; i < l; i++) {
                if (stack[i] === callback) {
                    stack.splice(i, 1);
                    return;
                }
            }
        };

        this.dispatchEvent = function(event) {
            if (!(event.type in this.listeners)) {
                return true;
            }
            var stack = this.listeners[event.type].slice();

            for (var i = 0, l = stack.length; i < l; i++) {
                stack[i].call(this, event);
            }
            return !event.defaultPrevented;
        };

        this.sync = function() {
            var beforeRequest = Date.now();
            var xhr = new XMLHttpRequest();
            xhr.open('GET', this.timeserver);

            xhr.onload = (function() {
                if (xhr.status === 200) {
                    console.log("Timeserver sync" + xhr.responseText);
                    var now = Date.now();
                    this.rtt = now - beforeRequest;
                    this.syncedTimestamp = new Date(xhr.responseText);
                    this.latency = this.syncedTimestamp.getTime() + (this.rtt / 2.0) - now;
                    var syncEventData = {
                        latency: this.latency,
                        servertime: this.syncedTimestamp,
                        localtime: now,
                        rtt: this.rtt
                    };
                    this.dispatchEvent(new Event("sync", syncEventData));
                } else {
                    console.log("Timeserver sync error " + xhr.status)
                    this.dispatchEvent(new Event("error", {
                        errorMessage: "Timeserver " + this.timeserver + ' has responded with status code ' + xhr.status
                    }));
                }

                if (this.interval > 0) {
                    if (this.synctimeout != undefined) {
                        clearTimeout(this.synctimeout);
                    }
                    this.synctimeout = setTimeout(function() {
                        this.sync();
                    }.bind(this), this.interval);
                }
            }.bind(this));
            xhr.send();
        }

        this.getLatency = function() {
            return this.latency;
        }

        this.getServerTime = function() {
            return new Date(Date.now() + this.latency);
        }

    }


    THEOplayer.LatencyManager = function(playerInstance, timeserver, configuration) {
        this.version = "1.0.3"
        //Event Listeners
        this.listeners = {};
        //THEOplayer instance
        this.player = playerInstance;
        //frequency of the update
        this.interval = 100;
        //instance of TimeServer must support timeserver.getServerTime() : Date()
        this.timeserver = timeserver;
        //target latency value the player must
        this.targetlatency = 5000;
        //window around targetlatency the manager will consider in sync
        this.latencywindow = 250;
        //window around targetlatency the manager considers to fire seek command rather than change playbackrate
        this.seekwindow = 2500;
        //the current latency of the player
        //== this.timeServer.getServerTime() - this.player.currentProgramDateTime
        this.currentlatency = 0;
        //If buffer is below this value, latencymanager will not act
        this.safebuffervalue = HAVE_ENOUGH_DATA;
        //maximum increase/decrease in speed
        this.ratechange = 0.08;
        //Allow the player to be early (currentatency < targetLatency is ok)
        this.allowearly = false;
        //when in this mode the manager will chase targetLatency +/- (latencyWindow / 2)
        this.catchingup = true;
        //manager is paused
        this.paused = false;
        //manager has started
        this._started = false;
        this._timeout = null;

        this.fireupdate = true;

        this.now = Date();
        this.servernow = Date();
        this.programDateTime = Date();
        this.enableoverlay = false;

        //lastseek is used to avoid firing multiple seek commands
        this._lastSeek = new Date();
        //seektimeout is used in combination with _lastseek
        //manager will not issue more than 1 seek command per _seekTimeout (in ms)
        this._seekTimeOut = 1000;
        this.currenthigh = 0;
        this.currentlow = 0;
        this.seekhigh = 0;
        this.seeklow = 0;
        this.pgdtrequest = 0;
        this.disableOnPause = false;
        this.sync = true;
        //additional fix to compensate for encoder/packager latency
        this.encoderlatency = 0;
        this.currentpackagerlatency = 0;
        this.currentpackagerDateTime = 0;
        this.currentencoderlatency = 0;
        this.encodertime = 0;
        this.useencodertime = false;
        this.SEEKTIMEOUT = 2000;
        this.isSynced = function() {
            return this.currentLow <= this.currentlatency &&
                this.currentlatency < this.currentHigh;

        }

        this.update = function() {
            if (!this.player.paused) {

                this.now = Date.now();
                this.programDateTime = this.player.currentProgramDateTime;

                this.pgdtrequest = Date.now() - this.now;
                this.servernow = this.timeserver.getServerTime();

                this.currentlatency = this.servernow - this.programDateTime;
                this.currentpackagerlatency = this.currentlatency;
                this.currentencoderlatency = this.currentlatency - this.encoderlatency;

                this.encodertime = this.programDateTime - this.encoderlatency;
                this.packagertime = this.programDateTime;

                if (!this.paused) {
                    this.currenthigh = this.targetlatency + this.latencywindow;
                    this.currentlow = this.targetlatency - this.latencywindow;
                    this.seekhigh = this.targetlatency + this.seekwindow;
                    this.seeklow = this.targetlatency - this.seekwindow;

                    if (this.catchingup) {
                        this.currenthigh = this.targetlatency + (this.latencywindow / 2);
                        this.currentlow = this.targetlatency - (this.latencywindow / 2);
                    }


                    if (this.currentlatency < this.seeklow) {
                        this._setpbr(1 - this.ratechange);
                        this._seekToSync();
                    } else if (this.currentlatency > this.seekhigh) {

                        this._setpbr(1 + this.ratechange);
                        this._seekToSync();
                    } else if (this.currentlatency > this.currenthigh) {
                        this.catchingup = true;
                        this._setpbr(1 + this.ratechange);
                    } else if (!this.allowearly && (this.currentlatency < this.currentlow)) {
                        this.catchingup = true;
                        this._setpbr(1 - this.ratechange);
                    } else {
                        this.catchingup = false;
                        this._setpbr(1);
                    }
                } else {
                    this._setpbr(1);
                }

                this.sendupdate();
            }

            if (this._started)
                this._timeout = setTimeout(this.update, this.interval);

        }.bind(this);

        this.sendupdate = function() {
            if (this.fireupdate) {
                var l = this.getLatency();
                theoplayerAndroid.sendMessage('THEOplayer.latencyManager.update', JSON.stringify(l));
            }
        }.bind(this);


        this.lastSeek = 0;
        //seek to the synchronisation point
        this._seekToSync = function() {
            var diff = ((this.timeserver.getServerTime() - this.player.currentProgramDateTime) - this.targetlatency) / 1000.0;
            this._seekTo(this.player.currentTime + diff);

        }.bind(this);

        this._lastSeek = 0;
        //seek to a specific time
        this._seekTo = function(time) {

            var currentSeek = new Date().getTime();

            if (!this.player.seeking && (currentSeek - this._lastSeek) > this.SEEKTIMEOUT) {
                this._lastSeek = new Date().getTime();
                this.player.currentTime = time;
                this.dispatchEvent(new CustomEvent("seek", {
                    detail: {
                        to: time
                    }
                }));
            }
        }.bind(this);

        //Evaluates whether the player has a buffer state that allows the manager to actively manage the player
        //to avoid buffer underruns
        this.hasSafeBuffer = function() {
            return this.player.readyState >= this.safebuffervalue
        }.bind(this)

        //set the players playbackrate if it is in a safe state
        this._setpbr = function(pbr) {

            if (this.player.playbackRate !== pbr) {
                if (pbr > 1) {
                    if (this.hasSafeBuffer()) {
                        var oldpbr = this.player.playbackRate;
                        this.player.playbackRate = pbr;
                        this.dispatchEvent(new CustomEvent("ratechange", {
                            detail: {
                                old: oldpbr,
                                new: pbr
                            }
                        }));
                    }
                } else {
                    var oldpbr = this.player.playbackRate;
                    this.player.playbackRate = pbr;
                    this.dispatchEvent(new CustomEvent("ratechange", {
                        detail: {
                            old: oldpbr,
                            new: pbr
                        }
                    }));
                }
            }
        }.bind(this);


        //start the manager
        this.start = function() {
            if (!this._started) {
                this._started = true;
                this._timeout = setTimeout(this.update, this.interval);
                this.dispatchEvent(new CustomEvent("started"));
            }
        }.bind(this);


        //stop the manager
        this.stop = function() {
            if (this._started) {
                clearTimeout(this._timeout)
                this._timeout = null;
                this._started = false;
                this.dispatchEvent(new CustomEvent("stopped"));
            }
        }.bind(this);


        this.configure = function(config) {
            if (typeof config === "string")
                config = JSON.parse(config);


            this.targetlatency = config.targetlatency || 5000;
            this.latencywindow = config.latencywindow || 250;
            this.seekwindow = config.seekwindow || 5000;
            this.interval = config.interval || 200;
            this.ratechange = config.ratechange || 0.08;
            this.fireupdate = config.fireupdate || true;
            this.disableOnPause = config.disableonpause || false;
            this.sync = config.sync || true;
            this.encoderlatency = config.encoderlatency || 0;
            this.useencodertime = config.useencodertime || false;


            if (THEOplayer.timeServer && (THEOplayer.timeServer.timeserver !== config.timeserver)) {
                THEOplayer.timeServer.timeserver = config.timeserver;
                THEOplayer.timeServer.sync();
            }
            this.pause(false);
        }.bind(this);


        this.getConfiguration = function() {
            return {
                targetlatency: this.targetlatency,
                latencywindow: this.latencywindow,
                seekwindow: this.seekwindow,
                interval: this.interval,
                ratechange: this.ratechange,
                fireupdate: this.fireupdate,
                sync: this.sync
            }
        }.bind(this);

        this.getLatency = function() {
            return {
                currentlatency: this.currentlatency,
                localtime: this.now,
                programdatetime: this.programDateTime.getTime(),
                servertime: this.servernow.getTime()
            }
        }.bind(this);

        this.getLatencyString = function() {
            return JSON.stringify(this.getLatency())
        }.bind(this);

        //listen for an event of type 'type'
        this.addEventListener = function(type, callback) {
            if (!(type in this.listeners)) {
                this.listeners[type] = [];
            }
            this.listeners[type].push(callback);
        };


        //remove eventlistener
        this.removeEventListener = function(type, callback) {
            if (!(type in this.listeners)) {
                return;
            }
            var stack = this.listeners[type];
            for (var i = 0, l = stack.length; i < l; i++) {
                if (stack[i] === callback) {
                    stack.splice(i, 1);
                    return;
                }
            }
        };

        //fire event
        this.dispatchEvent = function(event) {
            if (!(event.type in this.listeners)) {
                return true;
            }
            var stack = this.listeners[event.type].slice();

            for (var i = 0, l = stack.length; i < l; i++) {
                stack[i].call(this, event);
            }
            return !event.defaultPrevented;
        };

        this.pause = function(isPaused) {
            this.paused = isPaused;
            if (this._started) {
                if (!isPaused)
                    this._seekToSync();
            }
        };
        this.configure(configuration);
    }

    THEOplayer.initializeLatencyManager = function(playerInstance, configuration) {
        if (typeof configuration === "string")
            configuration = JSON.parse(configuration);

        var player = playerInstance;
        if (!player)
            player = THEOplayer.players[0];

        var ts = "https://time.akamai.com/?iso&ms";
        if (configuration.timeserver)
            ts = configuration.timeserver;

        var timeinterval = 300;
        if (configuration.timeinterval)
            timeinterval = configuration.timeinterval;


        THEOplayer.timeServer = new THEOplayer.TimeServer(ts, timeinterval);
        if (configuration.sync === undefined || configuration.sync === true)
            THEOplayer.timeServer.sync();


        THEOplayer.playingcount = 0;
        THEOplayer.latencyManager = new THEOplayer.LatencyManager(player, THEOplayer.timeServer, configuration);
        player.addEventListener("playing", function() {
            if (THEOplayer.playingcount === 0) {
                THEOplayer.timeServer.sync();
                THEOplayer.latencyManager.start();
                THEOplayer.latencyManager.pause(false);
            }
            THEOplayer.playingcount++;
        });

        player.addEventListener("sourcechange", function() {
            THEOplayer.latencyManager.stop();
            THEOplayer.playingcount = 0;
        });
    }

}());