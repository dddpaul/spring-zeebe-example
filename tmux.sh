#!/bin/sh

session="spring-zeebe"

attach() {
    [ -n "${TMUX:-}" ] &&
        tmux switch-client -t $session ||
        tmux attach-session -t $session
}

if tmux has-session -t "=$session" 2> /dev/null; then
    attach
    exit 0
fi


tmux new-session -d -s $session

# 6 horizontal panes and low command pane
tmux split-window -v
tmux resize-pane -U 10
tmux split-window -v
tmux resize-pane -U 10
tmux split-window -v
tmux resize-pane -U 10
tmux split-window -v
tmux resize-pane -U 10
tmux split-window -v
tmux resize-pane -U 10
tmux split-window -v
tmux resize-pane -U 10

# Run 6 workers
tmux select-pane -U
tmux send-keys -t $session 'make worker-bar' Enter
tmux select-pane -U
tmux send-keys -t $session 'make worker-bar' Enter
tmux select-pane -U
tmux send-keys -t $session 'make worker-bar' Enter
tmux select-pane -U
tmux send-keys -t $session 'make worker-bar' Enter
tmux select-pane -U
tmux send-keys -t $session 'make worker-bar' Enter
tmux select-pane -U
tmux send-keys -t $session 'make worker-bar' Enter

# Return to command pane
tmux select-pane -L
tmux select-pane -L
tmux select-pane -L
tmux select-pane -L
tmux select-pane -L
tmux select-pane -L

attach
