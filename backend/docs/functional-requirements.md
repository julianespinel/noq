# Functional Requirements

## Company

1. Create company, ex: Bank A

## Branch

1. Add branch to company, ex: Bank A at 33rd street

## Queue

1. Add queue to branch, ex: Bank A at 33rd street, payments queue

## Turn

### States

1. Requested: Take a turn, ex: add phone number to queue (enqueue)
1. Cancelled: Cancel turn, ex: remove user from queue: (cancel -> dequeue)
1. Ready: Notify turn is ready, ex: call user to come to be served, notify him/her (notify)
1. Started: Turn starts, ex: the user arrived to the branch (dequeue)
1. Ended: Turn ends, ex: the user left the branch after he/she was served (done)

## Reports

### General report per queue and date range

1. Show the average waiting time (from take turn to notify when turn is ready) on the current day
1. Show the canceled turns on the current day
1. Show the average time to wait for the customer (from turn ready to turn starts) on the current day
1. Show the average turn execution time (from turn starts to turn ends) on the current day
1. Show the users served (amount of turns ended) on the current day
