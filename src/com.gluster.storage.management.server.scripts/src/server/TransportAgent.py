import Commands
#from Common import log
from Protocol import *
from RequestHandler import *

def processRequest(requestDom):
    Globals.REQUEST_MAP = {
                   Commands.COMMAND_GET_SERVER_NETWORK_CONFIG    : {"handle":handleRequestGetServerNetworkConfig,
                                                                    "pre-run":False, "run":True, "post-run":False, \
                                                                    "cleanup":False, "sync-config":False, "safemode":False}}

    messageId = requestDom.getMessageId()
    if not messageId:
        log("invalid message from web agent")
        return None

    requestCommand = requestDom.getRequestCommand()
    if not requestCommand:
        log("invalid request from web agent")
        return None

    try:
        requestCommand = Globals.REQUEST_MAP[requestCommand]['handle']
    except KeyError: # Handler not found!
        return ResponseXml(requestCommand, "Invalid command", messageId, version)
    return requestCommand(requestDom)
