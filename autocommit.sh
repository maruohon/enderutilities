#!/bin/bash

AUTOCOMMITREPO="C:\Users\masa\Desktop\minecraft_modding\modjam\modjam4_autocommit.git\.git"
WORKDIR="C:\Users\masa\Desktop\minecraft_modding\modjam\modjam4.git"
TIMESTAMPFILE="${WORKDIR}/.autocommit.txt"

DONE="false"

while true
do
	M=`date "+%M"`
	S=`date "+%S"`

	# if [ "x${S}" = "x00" ]
	# then
	# 	# Commit quarter hourly
	# 	if [ "x${M}" = "x00" ] || [ "x${M}" = "x15" ] || [ "x${M}" = "x30" ] || [ "x${M}" = "x45" ]
	# 	then
	# 		TIMESTAMP=`date "+%Y-%m-%d_%H:%M:%S"`
	# 		echo "Auto-commit @ $TIMESTAMP" > ${TIMESTAMPFILE}
	# 		# echo "Timestamp: $TIMESTAMP"
	# 		git --work-tree "${WORKDIR}" --git-dir "${AUTOCOMMITREPO}" add -A .
	# 		git --work-tree "${WORKDIR}" --git-dir "${AUTOCOMMITREPO}" add -f "${TIMESTAMPFILE}"
	# 		git --work-tree "${WORKDIR}" --git-dir "${AUTOCOMMITREPO}" commit -m "Auto-commit @ ${TIMESTAMP}"
	# 	fi
	# fi
	# sleep 1

	# Commit quarter hourly
	if [ "x${M}" = "x00" ] || [ "x${M}" = "x15" ] || [ "x${M}" = "x30" ] || [ "x${M}" = "x45" ]
	then
		if [ "x${DONE}" == "xfalse" ]
		then
			TIMESTAMP=`date "+%Y-%m-%d_%H:%M:%S"`
			echo "Auto-commit @ $TIMESTAMP" > ${TIMESTAMPFILE}
			# echo "Timestamp: $TIMESTAMP"

			git --work-tree "${WORKDIR}" --git-dir "${AUTOCOMMITREPO}" add -A .
			git --work-tree "${WORKDIR}" --git-dir "${AUTOCOMMITREPO}" add -f "${TIMESTAMPFILE}"
			git --work-tree "${WORKDIR}" --git-dir "${AUTOCOMMITREPO}" commit -m "Auto-commit @ ${TIMESTAMP}"

			DONE="true"
		fi
	else
		DONE="false"
	fi

	sleep 30
done